package de.quinesoft.checklist.persistence

import de.quinesoft.checklist.model.ToDoItem

import scala.concurrent.Future

/**
 * @author <a href="mailto:krickl@quinesoft.de>Maximilian Krickl</a>
 */
object Store extends ChecklistStore {

  val cache: Map[Id, ToDoItem] = Map.empty

  override def add(newItem: ToDoItem): Future[Unit] = {
    cache + (newItem.id -> newItem)
    Future.successful(())
  }

  override def get(id: Id): Future[Option[ToDoItem]] = {
    Future.successful(cache.get(id))
  }

  override def delete(id: Store.Id): Future[Unit] = {
    Future.failed(new NotImplementedError("This operation is not yet implemented"))
  }

  override def keys(): Set[Id] = {
    cache.keySet
  }
}
