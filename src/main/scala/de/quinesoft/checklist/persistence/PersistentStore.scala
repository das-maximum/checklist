package de.quinesoft.checklist.persistence

import de.quinesoft.checklist.model.ToDoItem
import io.circe.Codec
import io.circe.generic.semiauto.deriveCodec

trait PersistentStore {
  def persist(item: PersistingToDoItem)
}

case class PersistingToDoItem(id: String, todo: Option[ToDoItem])

object PersistingToDoItem {
  implicit val persistingtodoItemCodec: Codec[PersistingToDoItem] = deriveCodec[PersistingToDoItem]
}
