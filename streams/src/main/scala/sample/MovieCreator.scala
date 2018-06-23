package sample

import akka.actor._
import akka.stream._
import scala.concurrent.{ExecutionContext, Future}
import java.nio.file._
import akka.NotUsed
import com.typesafe.scalalogging.StrictLogging
import model._
import akka.stream.scaladsl._
import akka.util.ByteString
import io.circe.syntax._
import io.circe.parser._
import client.MovieClient

object MovieCreator extends StrictLogging {
  def main(args: Array[String]): Unit = {
    implicit val system = ActorSystem("movie-creator")
    implicit val mat = ActorMaterializer()
    implicit val ec: ExecutionContext = ExecutionContext.global

    val movieClient = new MovieClient()

    val filePath = Paths.get("./movies.txt")

    val fileSource: Source[ByteString, Future[IOResult]] =
      FileIO.fromPath(filePath)

    val linesFlow = Flow[ByteString]
      .via(Framing.delimiter(ByteString("\n"), 1000))
      .map(_.utf8String)

    val moviesFlow = Flow[String]
      .map(decode[model.Movie])
      .mapConcat {
        case Left(error) =>
          logger.warn(s"Error during parsing: $error")
          Nil

        case Right(movie) =>
          List(movie)
      }

    val savingFlow = Flow[Movie]
      .mapAsyncUnordered(8) { movie =>
        movieClient createMovie movie
      }

    val printSink =
      Sink.foreach[String](savingResult => logger.info(s"Result: $savingResult"))

    val graph = fileSource
      .via(linesFlow)
      .via(moviesFlow)
      .via(savingFlow)
      .toMat(printSink)(Keep.right)

    val futureDone = graph.run()
    futureDone.onComplete { done =>
      logger.info(done.toString)
      system.terminate()
    }
  }
}

object GenerateFile extends App {
  val movies = 1 to 10 map { idx =>
    val id = idx * 100 + idx * 10 + idx * 2 + idx + 3
    Movie(id, s"Movie with id$id", 1, 90, "english", None, "UA")
  }

  val jsons = movies.map(_.asJson.noSpaces).mkString("\n")
  Files.write(Paths.get("./movies.txt"), jsons.getBytes())
}
