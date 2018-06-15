package repositories

import PostgresProfile.api._
import model._

class ReviewerTable(tag: Tag) extends Table[model.Reviewer](tag, "reviewer") {
  val id   = column[Int]("rev_id", O.PrimaryKey)
  val name = column[Option[String]]("rev_name")

  def * = (id, name) <> ((Reviewer.apply _).tupled, Reviewer.unapply)
}

object ReviewerTable {
  val query = TableQuery[ReviewerTable]
}