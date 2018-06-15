package model

import java.time.LocalDate
import io.circe.generic.JsonCodec

trait HasId {val id: Int}

@JsonCodec
case class Reviewer(id: Int, name: Option[String]) extends HasId

@JsonCodec
case class Rating(movieId: Int, reviewerId: Int, reviewStars: Option[Double], numberOfRatings: Option[Int])

@JsonCodec
case class Movie(id: Int, title: String, year: Int, time: Int, language: String, releaseDate: Option[LocalDate],
                 releaseCountry: String) extends HasId

object Movie {
  sealed trait Filter
  object Filter {
    case class Title(like: String) extends Filter
    case class Genre(like: String) extends Filter
    case class Director(like: String) extends Filter
    case class Rating(value: Double) extends Filter
  }

  @JsonCodec
  case class Info(
                   title: String,
                   releaseCountry: String,
                   releaseDate: Option[LocalDate],
                   genres: Vector[Genre],
                   directors: Vector[Director],
                   cast: Vector[Actor],
                   avgStars: Double
                 )
}

@JsonCodec
case class Genre(id: Int, title: String) extends HasId

@JsonCodec
case class MovieGenre(movieId: Int, genreId: Int)

@JsonCodec
case class Director(id: Int, firstName: String, lastName: String) extends HasId

@JsonCodec
case class MovieDirection(directorId: Int, movieId: Int)

@JsonCodec
case class Actor(id: Int, firstName: String, lastName: String, gender: String) extends HasId

@JsonCodec
case class MovieCast(actorId: Int, movieId: Int, role: String)
