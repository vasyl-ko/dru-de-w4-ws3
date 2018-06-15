package repositories

import PostgresProfile.api._
import model._

class GenreTable(tag: Tag) extends Table[model.Genre](tag, "genres") {
  val id = column[Int]("gen_id", O.PrimaryKey)
  val title = column[String]("gen_title")

  def * = (id, title) <> ((Genre.apply _).tupled, Genre.unapply)
}

object GenreTable {
  val query = TableQuery[GenreTable]
}