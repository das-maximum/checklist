package de.quinesoft.checklist.persistence

import de.quinesoft.checklist.model.ToDoItem

/**
 * @author <a href="mailto:krickl@quinesoft.de>Maximilian Krickl</a>
 */
trait ChecklistStore {
  def add(newItem: ToDoItem): Boolean

  def update(changedItem: ToDoItem): Boolean

  def delete(id: String): Unit

  def get(id: String): Option[ToDoItem]

  def getAll: Set[ToDoItem]

  def keys: Set[String]
}
