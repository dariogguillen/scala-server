package com.howtographql.scala.sangria

import akka.http.scaladsl.model.DateTime
import com.howtographql.scala.sangria.models._
import java.sql.Timestamp
import scala.concurrent.Await
import scala.concurrent.duration._
import scala.language.postfixOps
import slick.jdbc.H2Profile.api._

object DBSchema {
  implicit val dateTimeColumnType =
    MappedColumnType.base[DateTime, Timestamp](
      dt => new Timestamp(dt.clicks),
      ts => DateTime(ts.getTime)
    )

  class LinksTable(tag: Tag) extends Table[Link](tag, "LINK") {

    def id = column[Int]("ID", O.PrimaryKey, O.AutoInc)
    def url = column[String]("URL")
    def description = column[String]("DESCRIPTION")
    def createdAt = column[DateTime]("CREATED_AT")

    def * = (id, url, description, createdAt).mapTo[Link]
  }
  val Links = TableQuery[LinksTable]

  class UsersTable(tag: Tag) extends Table[User](tag, "USERS") {
    def id = column[Int]("ID", O.PrimaryKey, O.AutoInc)
    def name = column[String]("NAME")
    def email = column[String]("EMAIL")
    def password = column[String]("PASSWORD")
    def createdAt = column[DateTime]("CREATED_AT")

    def * = (id, name, email, password, createdAt).mapTo[User]
  }
  val Users = TableQuery[UsersTable]

  class VotesTable(tag: Tag) extends Table[Vote](tag, "VOTES") {
    def id = column[Int]("ID", O.PrimaryKey, O.AutoInc)
    def userId = column[Int]("USER_ID")
    def linkId = column[Int]("LINK_ID")
    def createdAt = column[DateTime]("CREATED_AT")

    def * = (id, userId, linkId, createdAt).mapTo[Vote]
  }
  val Votes = TableQuery[VotesTable]

  val databaseSetup = DBIO.seq(
    Links.schema.create,
    Links forceInsertAll Seq(
      Link(
        1,
        "http://howtographql.com",
        "Awesome community driven GraphQL tutorial",
        DateTime(2017, 9, 12)
      ),
      Link(
        2,
        "http://graphql.org",
        "Official GraphQL web page",
        DateTime(2017, 10, 1)
      ),
      Link(
        3,
        "https://facebook.github.io/graphql/",
        "GraphQL specification",
        DateTime(2017, 10, 2)
      )
    ),
    Users.schema.create,
    Users forceInsertAll Seq(
      User(1, "Dario Garcia", "dario@mail.com", "1234567890"),
      User(2, "Mauro Gonzalez", "mauro@mail.com", "1234567890"),
      User(3, "Rodrigo Rodriguez", "rodrigo@mail.com", "1234567890")
    ),
    Votes.schema.create,
    Votes forceInsertAll Seq(
      Vote(id = 1, userId = 1, linkId = 3),
      Vote(id = 2, userId = 2, linkId = 3)
    )
  )

  def createDatabase: DAO = {
    val db = Database.forConfig("h2mem")

    Await.result(db.run(databaseSetup), 10 seconds)

    new DAO(db)

  }

}
