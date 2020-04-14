package de.quinesoft.checklist.persistence

import akka.Done
import de.quinesoft.checklist.model.ToDoItem

import scala.concurrent.Future

/**
 * @author <a href="mailto:krickl@quinesoft.de>Maximilian Krickl</a>
 */
object MapStore extends ChecklistStore {

  var cache: List[ToDoItem] = List(
    ToDoItem(text = "Milk"),
    ToDoItem(text = "Coffee"),
    ToDoItem(text = "Dish washer tabs"),
    ToDoItem(text = "Tooth paste"),
    ToDoItem(text = "Cheese")
  )

  override def done(id: String): Future[Done] = {
    cache = cache.map(item => if (item.id == id) item.copy(done = true) else item)
    Future.successful(Done)
  }

  override def add(newItem: ToDoItem): Future[Done] = {
    cache = newItem :: cache
    Future.successful(Done)
  }

  override def get(id: String): Future[Option[ToDoItem]] =
    Future.successful(cache.find(item => item.id == id))

  override def getAll: Future[List[ToDoItem]] = Future.successful(cache)

  override def delete(id: String): Future[Done] = {
    cache = cache.filterNot(item => item.id == id)
    Future.successful(Done)
    //Future.failed(new NotImplementedError("This operation is not yet implemented"))
  }

  override def keys(): Future[Set[String]] =
    Future.successful(cache.map(item => item.id).toSet)
}
