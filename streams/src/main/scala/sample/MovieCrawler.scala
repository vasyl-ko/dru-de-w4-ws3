package sample

import akka.actor._
import akka.stream._
import akka.stream.scaladsl._

import scala.concurrent.{ExecutionContext, Future}
import java.nio.file._

import akka.NotUsed
import client.MovieClient
import com.typesafe.scalalogging.StrictLogging
import io.circe.syntax._
import model._

import scala.concurrent.duration._

object MovieCrawler extends StrictLogging {
  type Request = None.type
  def main(args: Array[String]): Unit = {
    implicit val system = ActorSystem("movie-creator")
    implicit val mat = ActorMaterializer(
      ActorMaterializerSettings(system)
        .withSupervisionStrategy({ e =>
          logger.warn(
            s"Exception during stream processing: ${e.getLocalizedMessage}")
          Supervision.Resume
        })
    )

    implicit val ec: ExecutionContext = ExecutionContext.global

    val movieClient = new MovieClient()

    // todo: continually fetch all movies from API and get all info about them

    scala.io.StdIn.readLine("Press something to stop")
    system.terminate()
  }
}
