package repositories

import PostgresProfile.api._
import model._

class MovieGenreTable(tag: Tag) extends Table[model.MovieGenre](tag, "movie_genres") {
  val movieId = column[Int]("mov_id")
  val genreId = column[Int]("gen_id")

  def * = (movieId, genreId) <> ((MovieGenre.apply _).tupled, MovieGenre.unapply)

  val movieIdForeignKey = foreignKey(
    "mov_id_fk", movieId, MovieTable.query)(
    _.id, ForeignKeyAction.Cascade, ForeignKeyAction.Cascade
  )
  val genreIdForeignKey = foreignKey(
    "gen_id_fk", genreId, GenreTable.query)(
    _.id, ForeignKeyAction.Cascade, ForeignKeyAction.Cascade
  )
}

object MovieGenreTable {
  val query = TableQuery[MovieGenreTable]
}