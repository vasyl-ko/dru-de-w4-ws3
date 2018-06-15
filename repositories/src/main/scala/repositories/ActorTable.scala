package repositories

import PostgresProfile.api._
import model._

class ActorTable(tag: Tag) extends Table[model.Actor](tag, "actor") {
  val id        = column[Int]("act_id", O.PrimaryKey)
  val firstName = column[String]("act_firstName")
  val lastName  = column[String]("act_lastName")
  val gender    = column[String]("act_gender")

  def * = (id, firstName, lastName, gender) <> ((Actor.apply _).tupled, Actor.unapply)

}

object ActorTable {
  val query = TableQuery[ActorTable]
}