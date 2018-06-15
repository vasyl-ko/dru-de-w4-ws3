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

  def getAllMovies: Future[Vector[Movie]] = ???
  def getAllMovies(
      titleOpt: Option[String] = None,
      directorOpt: Option[String] = None,
      genreOpt: Option[String] = None,
      avgRatingOpt: Option[Double] = None
  ): Future[Vector[Movie]] = ???

  def createMovie(movie: Movie): Future[String] = ???
}
