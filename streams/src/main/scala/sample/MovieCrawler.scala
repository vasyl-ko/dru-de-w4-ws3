package sample

import akka.actor._
import akka.stream._
import akka.stream.scaladsl._
import scala.concurrent.{ExecutionContext, Future}
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

    val tickSource = Source
      .tick(1.milli, 1.second, None)
      .mapAsyncUnordered(8) { _ =>
        movieClient.getAllMovies
      }
      .mapConcat(identity)

    val loggingSink = Sink.foreach[String](message => logger.info(message))

    val infoFlow = Flow[model.Movie]
      .throttle(1, 3.second)
      .mapAsyncUnordered(8) { movie =>
        movieClient.getInfo(movie.id)
      }

    val printMovieFlow = Flow[model.Movie].map(_.toString)
    val printInfoFlow = Flow[model.Movie.Info].map(_.toString)

    val complexFlow: Flow[model.Movie, String, NotUsed] =
      Flow.fromGraph(GraphDSL.create() { implicit builder =>
        import GraphDSL.Implicits._

        val broadcast = builder.add(Broadcast[model.Movie](2))
        val merge = builder.add(Merge[String](2))

        broadcast.out(0) ~> printMovieFlow ~> merge.in(0)
        broadcast.out(1) ~> infoFlow ~> printInfoFlow ~> merge.in(1)

        FlowShape(broadcast.in, merge.out)
      })

    val futureDone = tickSource.via(complexFlow).runWith(loggingSink)

    scala.io.StdIn.readLine("Press something to stop")
    system.terminate()
  }
}
