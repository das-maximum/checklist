package de.quinesoft.checklist.routes

import de.quinesoft.checklist.routes.Endpoints._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import de.quinesoft.checklist.persistence.ChecklistStore
import sttp.tapir.server.akkahttp.AkkaHttpServerInterpreter

import scala.concurrent.Future.successful
import scala.concurrent.{ExecutionContext, Future}

final class Routes(store: ChecklistStore)(implicit val ec: ExecutionContext) {
  import AkkaHttpServerInterpreter.toRoute

  val routes: Route = {
    concat {
      // get all ids
      toRoute(getIds)(token => {
        successful(Right(store.keys))
      }) ~
      // get single item
      toRoute(getItem)(tokenAndUuid => {
        val uuid = tokenAndUuid._2.toString

        successful {
          store.get(uuid) match {
            case Some(value) => Right(value)
            case None => Left("No such item")
          }
        }
      }) ~
      toRoute(newItem)(tokenAndText => {
        val text = tokenAndText._2

        successful {
          store.add(text) match {
            case Some(value) => Right(value)
            case None => Left("No such item")
          }
        }
      }) ~
      toRoute(updateItem)(tokenAndUuidAndToDoItem => {
        val updatedItem = tokenAndUuidAndToDoItem._3
        successful {
          if (store.update(updatedItem)) {
            Right("Successfully updated")
          } else {
            Left("Unknown item")
          }
        }
      }) ~
      toRoute(deleteItem)(tokenAndUuid => {
        val uuid = tokenAndUuid._2.toString

        successful {
          store.delete(uuid)
          Right("Item deleted")
        }
      }) ~
      toRoute(getAllTodos)(token => {
        successful {
          Right(store.getAll)
        }
      }) ~
      toRoute(version)( _=> {
        successful {
          Right("0.0.2")
        }
      })
    }
  }
}


