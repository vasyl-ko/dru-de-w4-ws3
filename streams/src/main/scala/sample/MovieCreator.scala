package sample

import akka.actor._
import akka.stream._
import scala.concurrent.{ExecutionContext, Future}
import java.nio.file._
import com.typesafe.scalalogging.StrictLogging
import io.circe.syntax._
import model._

object MovieCreator extends StrictLogging {
  def main(args: Array[String]): Unit = {
    implicit val system = ActorSystem("movie-creator")
    implicit val mat = ActorMaterializer()
    implicit val ec: ExecutionContext = ExecutionContext.global

    val filePath = Paths.get("./movies.txt")
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
