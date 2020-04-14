package de.quinesoft.checklist.routes

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import de.quinesoft.checklist.model.ToDoItem
import de.quinesoft.checklist.persistence.{ChecklistStore, MapStore}
import de.quinesoft.checklist.model.ToDoItemJsonProtocol._
import spray.json.DefaultJsonProtocol

/**
 * @author <a href="mailto:krickl@quinesoft.de>Maximilian Krickl</a>
 */
object Routing extends SprayJsonSupport with DefaultJsonProtocol {

  val store: ChecklistStore = MapStore

  val routes: Route = pathPrefix("api") {
    path("todo") {
      get {
        parameter('id.?) {
          case Some(id) => complete(store.get(id))
          case None =>     complete((StatusCodes.OK, store.keys))
        }
      } ~
      post {
        entity(as[String]) {
          itemText => complete(store.add(ToDoItem(text = itemText)))
        }
      } ~
      delete {
        parameter('id) {
          id => complete(store.delete(id))
        }
      }
    } ~
    path("todo" / "full") {
      get {
        complete(store.getAll)
      }
    } ~
    path("done") {
      get {
        parameter('id) {
          id => complete((StatusCodes.OK, store.done(id)))
        }
      }
    }
  }
}
