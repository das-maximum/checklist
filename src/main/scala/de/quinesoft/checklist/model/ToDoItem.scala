package de.quinesoft.checklist.model

import io.circe.Codec
import io.circe.generic.semiauto.deriveCodec

import java.time.Instant
import java.util.UUID

/**
 * @author <a href="mailto:krickl@quinesoft.de>Maximilian Krickl</a>
 */
case class ToDoItem(id: String, text: String, created: Instant, done: Boolean, deleted: Boolean)
object ToDoItem {
  def apply(text: String): ToDoItem = {
    ToDoItem(
      UUID.randomUUID().toString,
      text,
      Instant.now(),
      done = false,
      deleted = false,
    )
  }

  implicit val todoitemCodec: Codec[ToDoItem] = deriveCodec
}