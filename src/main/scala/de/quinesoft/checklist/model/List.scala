package de.quinesoft.checklist.model

import io.circe.Codec
import io.circe.generic.semiauto.deriveCodec

import java.time.Instant

case class ToDoList(name: String, created: Instant, items: List[ToDoItem])
object ToDoList {
  implicit val todolistCodec: Codec[ToDoList] = deriveCodec
}
