package de.quinesoft.checklist.model

import enumeratum._
import io.circe.Codec
import io.circe.generic.semiauto._

import scala.collection.immutable

sealed trait Role extends EnumEntry
object Role extends Enum[Role] with CirceEnum[Role] {

  case object Admin extends Role
  case object User extends Role

  override def values: immutable.IndexedSeq[Role] = findValues
}

case class User(name: String, password: String, lists: List[ToDoList], role: Role)
object User {
  implicit val userCodec: Codec[User] = deriveCodec[User]
}