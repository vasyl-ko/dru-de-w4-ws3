package repositories

import com.github.tminglei.slickpg._

trait PostgresProfile
  extends ExPostgresProfile
    with PgDate2Support {

  object Api extends API with DateTimeImplicits {
  }

  override val api = Api
}

object PostgresProfile extends PostgresProfile
