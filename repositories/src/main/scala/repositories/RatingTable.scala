package repositories

import PostgresProfile.api._
import model._

class RatingTable(tag: Tag) extends Table[model.Rating](tag, "rating") {
  val movieId         = column[Int]("mov_id")
  val reviewerId      = column[Int]("rev_id")
  val reviewStars     = column[Option[Double]]("rev_stars")
  val numberOfRatings = column[Option[Int]]("num_o_rating")

  def * = (movieId, reviewerId, reviewStars, numberOfRatings) <> ((Rating.apply _).tupled, Rating.unapply)

  val movieIdForeignKey    = foreignKey(
    "mov_id_fk", movieId, MovieTable.query)(
    _.id, ForeignKeyAction.Cascade, ForeignKeyAction.Cascade
  )
  val reviewerIdForeignKey = foreignKey(
    "rev_id_fk", reviewerId, ReviewerTable.query)(
    _.id, ForeignKeyAction.Cascade, ForeignKeyAction.Cascade
  )
}

object RatingTable {
  val query = TableQuery[RatingTable]
}