package com.howtographql.scala.sangria

import sangria.schema.{Field, ListType, ObjectType}
import models._

import akka.http.scaladsl.model.DateTime
import sangria.schema._
import sangria.macros.derive._
import sangria.execution.deferred.{DeferredResolver, Fetcher, HasId}
import sangria.ast.StringValue

object GraphQLSchema {
  implicit val GraphQLDateTime = ScalarType[DateTime](
    "DateTime",
    coerceOutput = (dt, _) => dt.toString,
    coerceInput = {
      case StringValue(dt, _, _) =>
        DateTime.fromIsoDateTimeString(dt).toRight(DateTimeCoerceViolation)
      case _ => Left(DateTimeCoerceViolation)
    },
    coerceUserInput = {
      case s: String =>
        DateTime.fromIsoDateTimeString(s).toRight(DateTimeCoerceViolation)
      case _ => Left(DateTimeCoerceViolation)
    }
  )
  implicit val LinkType = deriveObjectType[Unit, Link](
    ReplaceField(
      "createdAt",
      Field("createdAt", GraphQLDateTime, resolve = _.value.createdAt)
    )
  )
  implicit val linkHasId = HasId[Link, Int](_.id)

  val Id = Argument("id", IntType)
  val Ids = Argument("ids", ListInputType(IntType))

  val linksFetcher = Fetcher(
    (ctx: MyContext, ids: Seq[Int]) => ctx.dao.getLinks(ids)
  )
  val Resolver = DeferredResolver.fetchers(linksFetcher)

  val QueryType = ObjectType(
    "Query",
    fields[MyContext, Unit](
      Field("allLinks", ListType(LinkType), resolve = c => c.ctx.dao.allLinks),
      Field(
        "link",
        OptionType(LinkType),
        arguments = Id :: Nil,
        resolve = c => linksFetcher.deferOpt(c.arg(Id))
      ),
      Field(
        "links",
        ListType(LinkType),
        arguments = Ids :: Nil,
        resolve = c => linksFetcher.deferSeq(c.arg(Ids))
      )
    )
  )

  val SchemaDefinition = Schema(QueryType)
}
