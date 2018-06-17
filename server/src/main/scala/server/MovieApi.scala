package server

import repositories.MovieRepository
import akka.http.scaladsl.model._
import akka.http.scaladsl.server._
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport
import io.circe._
import io.circe.syntax._

class MovieApi(movieRepository: MovieRepository)
    extends Directives
    with FailFastCirceSupport {
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
        complete(moviesToJson(movies))
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

  def routes: Route =
    index ~ findAllMovies ~ createFilm ~ findAllByFilters

  def moviesToJson(movies: Vector[model.Movie]): Json = {
    movies.asJson
  }
}
