package de.quinesoft.checklist.persistence

import akka.Done
import de.quinesoft.checklist.model.ToDoItem

import scala.concurrent.Future

/**
 * @author <a href="mailto:krickl@quinesoft.de>Maximilian Krickl</a>
 */
trait ChecklistStore {
  def add(newItem: String): Future[ToDoItem]

  def update(changedItem: ToDoItem): Future[Done]

  def delete(id: String): Future[Done]

  def get(id: String): Future[Option[ToDoItem]]

  def getAll: Future[List[ToDoItem]]

  def keys: Future[Set[String]]
}
