package client

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.http.scaladsl.unmarshalling._
import akka.stream.ActorMaterializer
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport
import model._
import scala.concurrent.{ExecutionContext, Future}
import io.circe.syntax._

class MovieClient(
    implicit system: ActorSystem,
    materializer: ActorMaterializer,
    ec: ExecutionContext
) extends FailFastCirceSupport {

  private val http = Http()
  def getAllMovies(
      title: String,
//                    directorOpt: Option[String] = None,
//                    genreOpt: Option[String] = None,
      avgRatingOpt: Option[Double] = None
  ): Future[Vector[Movie]] = {
    val uri: Uri = Uri("http://localhost:8080/movies/all/filter")
      .withQuery(
        Uri.Query(
          Seq(
            "title" -> title
          ) ++ avgRatingOpt.map(rate => "rating" -> rate.toString): _*)
      )

    val response = http.singleRequest(
      HttpRequest(
        HttpMethods.GET,
        uri
      )
    )
    response.flatMap {
      case HttpResponse(StatusCodes.OK, _, responseEntity, _) =>
        Unmarshal(responseEntity).to[Vector[Movie]]
    }
  }

  def getAllMovies: Future[Vector[Movie]] = {
    val response = http.singleRequest(
      HttpRequest(
        HttpMethods.GET,
        "http://localhost:8080/movies/all"
      )
    )
    response.flatMap {
      case HttpResponse(StatusCodes.OK, _, responseEntity, _) =>
        Unmarshal(responseEntity).to[Vector[Movie]]
    }
  }

  def createMovie(movie: Movie): Future[String] = {
    val response = http.singleRequest(
      HttpRequest(
        HttpMethods.POST,
        "http://localhost:8080/movies/create",
        entity = HttpEntity(
          ContentTypes.`application/json`,
          movie.asJson.noSpaces
        )
      )
    )
    response.map(_.toString())
  }
}
