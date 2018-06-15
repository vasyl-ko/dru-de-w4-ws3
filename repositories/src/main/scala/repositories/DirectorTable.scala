package repositories

import PostgresProfile.api._
import model._

class DirectorTable(tag: Tag) extends Table[model.Director](tag, "director") {
  val id        = column[Int]("dir_id", O.PrimaryKey)
  val firstName = column[String]("dir_fname")
  val lastName  = column[String]("dir_lname")

  def * = (id, firstName, lastName) <> ((Director.apply _).tupled, Director.unapply)

}

object DirectorTable {
  val query = TableQuery[DirectorTable]
}