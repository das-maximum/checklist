package de.quinesoft.checklist.persistence

import de.quinesoft.checklist.model.ToDoItem
import de.quinesoft.checklist.model.ToDoItemJsonProtocol._
import spray.json.{DefaultJsonProtocol, RootJsonFormat}

trait PersistentStore {
  def persist(item: PersistingToDoItem)
}

case class PersistingToDoItem(id: String, todo: Option[ToDoItem])

object PersistingToDoItem extends DefaultJsonProtocol {
  implicit val persistingToDoItemJsonProtocol: RootJsonFormat[PersistingToDoItem] = rootFormat(jsonFormat2(PersistingToDoItem.apply))
}