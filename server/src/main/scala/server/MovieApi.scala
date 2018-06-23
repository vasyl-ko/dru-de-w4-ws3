package server

import repositories.MovieRepository
import akka.http.scaladsl.model._
import akka.http.scaladsl.server._
import com.typesafe.scalalogging.StrictLogging
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport
import scala.concurrent.{ExecutionContext, Future}
import akka.stream.scaladsl._
import akka.http.scaladsl.model.ws._
import io.circe.parser._

class MovieApi(movieRepository: MovieRepository)(implicit ec: ExecutionContext)
    extends Directives
    with FailFastCirceSupport
    with StrictLogging {
  def index: Route =
    pathSingleSlash {
      get {
        complete("Hello")
      }
    } ~ path("index") {
      get {
        val html =
          """
          <html>
          <h1>HELLO, AKKA</h1>
          </html>
        """
        val response = HttpResponse(
          entity = HttpEntity(
            ContentTypes.`text/html(UTF-8)`,
            html
          )
        )
        complete(response)
      }
    }

  def findAllMovies: Route =
    path("movies" / "all") {
      val futureMovies = movieRepository.findAll()
      onSuccess(futureMovies) { movies =>
        complete(movies)
      }
    }

  def createFilm: Route =
    path("movies" / "create") {
      entity(as[model.Movie]) { movie =>
        val insertFuture = movieRepository.insert(movie)
        onComplete(insertFuture) {
          case scala.util.Success(1) => complete("OK")
          case scala.util.Success(0) => complete("NOT INSERTED")
          case scala.util.Failure(_: org.postgresql.util.PSQLException) =>
            complete(
              HttpResponse(
                status = StatusCodes.BadRequest,
                entity = "Bad record"
              )
            )
        }
      }
    }

  def findAllByFilters: Route =
    path("movies" / "all" / "filter") {
      parameters('title, 'rating.as[Double].?) { (title, rating) =>
        val filters: List[model.Movie.Filter] =
          List(model.Movie.Filter.Title(title)) ++ rating.map(
            model.Movie.Filter.Rating)

        val futureMovies = movieRepository.findAll(filters)
        onSuccess(futureMovies) { movies =>
          complete(movies)
        }
      }
    }

  def movieInfo: Route = path("movies" / IntNumber / "info") { movieId =>
    onSuccess(movieRepository.getInfo(movieId)) {
      case Some(movie) => complete(movie)
      case None =>
        complete(
          HttpResponse(
            status = StatusCodes.NotFound,
            entity = s"Not found movie with id $movieId"
          )
        )
    }
  }

  def websocket: Route = path("movies" / "ws") {
    handleWebSocketMessages(wsFlow)
  }

  private val wsFlow = {
    val messagesFlow = Flow[Message]
      .map(_.asTextMessage)
      .collect {
        case TextMessage.Strict(textMessage) => textMessage
      }

    val parsingFlow = Flow[String]
      .map(decode[model.Command])
      .mapAsync(2) {
        case Left(error) =>
          Future.successful(model.WsResponse.InvalidCommand(error.getMessage))

        case Right(model.Command.Ping(num)) =>
          Future.successful(model.WsResponse.Pong(num))

        case Right(model.Command.CreateMovie(movie)) =>
          movieRepository
            .insert(movie)
            .map { insertResult =>
              model.WsResponse.CreateMovieResponse(
                s"Insert result: $insertResult")
            }
      }

    import io.circe.syntax._

    val writingFlow = Flow[model.WsResponse]
      .map(_.asJson.spaces2)
      .map(TextMessage(_))

    messagesFlow via parsingFlow via writingFlow
  }

  def routes: Route =
    index ~
      findAllMovies ~
      createFilm ~
      findAllByFilters ~
      movieInfo ~
      websocket
}
