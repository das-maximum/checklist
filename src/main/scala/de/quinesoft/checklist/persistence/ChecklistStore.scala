package de.quinesoft.checklist.persistence

import de.quinesoft.checklist.model.ToDoItem

import scala.concurrent.Future

/**
 * @author <a href="mailto:krickl@quinesoft.de>Maximilian Krickl</a>
 */
trait ChecklistStore {
  type Id = String

  def add(newItem: ToDoItem): Future[Unit]

  def delete(id: Id): Future[Unit]

  def get(id: Id): Future[Option[ToDoItem]]

  def keys(): Set[Id]
}
