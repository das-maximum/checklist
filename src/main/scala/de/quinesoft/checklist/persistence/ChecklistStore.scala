package de.quinesoft.checklist.persistence

import de.quinesoft.checklist.model.ToDoItem

/**
 * @author <a href="mailto:krickl@quinesoft.de>Maximilian Krickl</a>
 */
trait ChecklistStore {
  def add(newItem: String): Option[ToDoItem]

  def update(changedItem: ToDoItem): Boolean

  def delete(id: String): Unit

  def get(id: String): Option[ToDoItem]

  def getAll: List[ToDoItem]

  def keys: Set[String]
}
