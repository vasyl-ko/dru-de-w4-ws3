package repositories

import PostgresProfile.api._
import model._

class MovieDirectionTable(tag: Tag) extends Table[model.MovieDirection](tag, "movie_direction") {
  val directorId = column[Int]("dir_id")
  val movieId    = column[Int]("mov_id")

  def * = (directorId, movieId) <> ((MovieDirection.apply _).tupled, MovieDirection.unapply)

  val directorIdForeignKey = foreignKey(
    "dir_id_fk", directorId, DirectorTable.query)(
    _.id, ForeignKeyAction.Cascade, ForeignKeyAction.Cascade
  )
  val movieIdForeignKey    = foreignKey(
    "mov_id_fk", movieId, MovieTable.query)(
    _.id, ForeignKeyAction.Cascade, ForeignKeyAction.Cascade
  )
}

object MovieDirectionTable {
  val query = TableQuery[MovieDirectionTable]
}