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


  override def update(changedItem: ToDoItem): Future[Done] = {
    cache = cache.map(item => if (item.id == changedItem.id) changedItem else item)
    Future.successful(Done)
  }

  override def add(newItem: String): Future[ToDoItem] = {
    val newToDo = ToDoItem(text = newItem)
    cache = newToDo :: cache
    Future.successful(newToDo)
  }

  override def get(id: String): Future[Option[ToDoItem]] =
    Future.successful(cache.find(item => item.id == id))

  override def getAll: Future[List[ToDoItem]] = Future.successful(cache)

  override def delete(id: String): Future[Option[String]] = {
    val tmpCache = cache.filterNot(item => item.id == id)
    if (tmpCache.length == cache.length) {
      Future.successful(None)
    } else {
      cache = tmpCache
      Future.successful(Some(id))
    }
  }

  override def keys(): Future[Set[String]] =
    Future.successful(cache.map(item => item.id).toSet)
}
