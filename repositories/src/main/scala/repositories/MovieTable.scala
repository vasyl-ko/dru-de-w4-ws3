package repositories

import PostgresProfile.api._
import java.time.LocalDate
import model._
import scala.concurrent.{ExecutionContext, Future}


class MovieTable(tag: Tag) extends Table[model.Movie](tag, "movie") with BaseTable[Movie] {
  val id             = column[Int]("mov_id", O.PrimaryKey)
  val title          = column[String]("mov_title")
  val year           = column[Int]("mov_year")
  val time           = column[Int]("mov_time")
  val language       = column[String]("mov_lang")
  val releaseDate    = column[Option[LocalDate]]("mov_dt_rel")
  val releaseCountry = column[String]("mov_rel_country")

  def * = (id, title, year, time, language, releaseDate, releaseCountry) <> ((Movie.apply _).tupled, Movie.unapply)
}

object MovieTable {
  val query = TableQuery[MovieTable]
}

class MovieRepository(val db: Database)(implicit ec: ExecutionContext) extends BaseRepository[Movie](MovieTable.query) {
  def createSchema(): Future[Unit] = {
    db.run((
      MovieTable.query.schema ++
        ActorTable.query.schema ++
        DirectorTable.query.schema ++
        GenreTable.query.schema ++
        ReviewerTable.query.schema ++
        MovieCastTable.query.schema ++
        MovieDirectionTable.query.schema ++
        MovieGenreTable.query.schema ++
        RatingTable.query.schema
      ).create
    )
  }

  def findAll(): Future[Vector[Movie]] = db.run(MovieTable.query.to[Vector].result)

  def findAll(filters: List[Movie.Filter]): Future[Vector[Movie]] = {
    val resultingQuery = filters.foldLeft[Query[MovieTable, Movie, Seq]](MovieTable.query) {
      case (query, Movie.Filter.Title(titleLike)) =>
        query.filter(_.title like s"%$titleLike%")

      case (query, Movie.Filter.Director(directorLike)) =>
        val matchedDirectorsQuery = DirectorTable.query.filter { director =>
          director.firstName.like(s"%$directorLike%") ||
            director.lastName.like(s"%$directorLike%")
        }
        val movieIds = matchedDirectorsQuery.join(MovieDirectionTable.query).on(_.id === _.directorId).map(_._2.movieId).distinct

        query.filter(_.id in movieIds)

      case (query, Movie.Filter.Rating(ratingValue)) =>
        for {
          movie <- query
          avgRating = RatingTable.query.filter(_.movieId === movie.id)
            .map(_.reviewStars.getOrElse(0.0))
            .avg.getOrElse(0.0)
          if avgRating >= ratingValue
        } yield movie


      case (query, Movie.Filter.Genre(genreLike)) =>
        val matchedGenres = GenreTable.query.filter(_.title like s"%$genreLike%")

        val movieIds = matchedGenres.join(MovieGenreTable.query).on(_.id === _.genreId).map(_._2.movieId).distinct

        query.filter(_.id in movieIds)
    }

    db.run(resultingQuery.to[Vector].result)
  }

  def getInfo(movieId: Int): Future[Option[Movie.Info]] = {
    val movieQuery = MovieTable.query.filter(_.id === movieId).map(mv => (mv.title, mv.releaseCountry, mv.releaseDate))
    val infoAction = movieQuery.result.headOption.flatMap {
      case None                                       => DBIO.successful(None)
      case Some((title, releaseCountry, releaseDate)) =>
        val movieGenresQuery = MovieGenreTable.query.filter(_.movieId === movieId)
          .join(GenreTable.query).on(_.genreId === _.id).map(_._2).to[Vector]

        val movieDirectionQuery = MovieDirectionTable.query.filter(_.movieId === movieId)
          .join(DirectorTable.query).on(_.directorId === _.id).map(_._2).to[Vector]

        val movieCastQuery = MovieCastTable.query.filter(_.movieId === movieId)
          .join(ActorTable.query).on(_.actorId === _.id).map(_._2).to[Vector]

        val avgRating = RatingTable.query.filter(_.movieId === movieId)
          .map(_.reviewStars.getOrElse(0.0))
          .avg.getOrElse(0.0)

        DBIO.successful((title, releaseCountry, releaseDate))
          .zip(movieGenresQuery.result)
          .zip(movieDirectionQuery.result)
          .zip(movieCastQuery.result)
          .zip(avgRating.result)
          .map(Some(_))
    }

    db.run(infoAction).map(
      _.map {
        case (((((title, releaseCountry, releaseDate), movieGenres), movieDirection), movieCast), avgRating) =>
          Movie.Info(title, releaseCountry, releaseDate, movieGenres, movieDirection, movieCast, avgRating)
      }
    )
  }
}