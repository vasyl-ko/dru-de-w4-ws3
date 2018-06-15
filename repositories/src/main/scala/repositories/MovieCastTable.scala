package repositories

import PostgresProfile.api._
import model._


class MovieCastTable(tag: Tag) extends Table[model.MovieCast](tag, "movie_cast") {
  val actorId = column[Int]("act_id")
  val movieId = column[Int]("mov_id")
  val role    = column[String]("role")

  def * = (actorId, movieId, role) <> ((MovieCast.apply _).tupled, MovieCast.unapply)

  val actorIdForeignKey = foreignKey(
    "act_id_fk", actorId, ActorTable.query)(
    _.id, ForeignKeyAction.Cascade, ForeignKeyAction.Cascade
  )
  val movieIdForeignKey = foreignKey(
    "mov_id_fk", movieId, MovieTable.query)(
    _.id, ForeignKeyAction.Cascade, ForeignKeyAction.Cascade
  )
}

object MovieCastTable {
  val query = TableQuery[MovieCastTable]
}