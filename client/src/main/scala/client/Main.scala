package client

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.concurrent.Await

object Main {
  def main(args: Array[String]): Unit = {
    implicit val system = ActorSystem("movie-client")
    implicit val materializer = ActorMaterializer()
    val client = new MovieClient()

    val allMovies = Await.result(
      client.getAllMovies(title = "Test", avgRatingOpt = None)
      /*.createMovie(
        model.Movie(
          2126,
          "Scala movie",
          -1,
          -1,
          "scala",
          None,
          "UA"
        ))*/,
      5.seconds
    )

    println(allMovies)

    Await.result(system.terminate(), 1.second)
  }
}
