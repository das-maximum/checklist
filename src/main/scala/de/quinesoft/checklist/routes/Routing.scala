package de.quinesoft.checklist.routes

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model.{HttpResponse, StatusCodes}
import akka.http.scaladsl.model.HttpMethods._
import akka.http.scaladsl.model.headers.{`Access-Control-Allow-Credentials`, `Access-Control-Allow-Headers`, `Access-Control-Allow-Methods`, `Access-Control-Allow-Origin`}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.{Directive0, Route}
import com.typesafe.scalalogging.Logger
import de.quinesoft.checklist.model.ToDoItem
import de.quinesoft.checklist.model.ToDoItemJsonProtocol._
import de.quinesoft.checklist.persistence.{ChecklistStore, MapStore}
import spray.json.DefaultJsonProtocol

/**
 * @author <a href="mailto:krickl@quinesoft.de>Maximilian Krickl</a>
 */
object Routing extends SprayJsonSupport with DefaultJsonProtocol with CORSHandler {

  private val logger: Logger = Logger(Routing.getClass.getCanonicalName)
  val store: ChecklistStore = MapStore

  def routes: Route = pathPrefix("api") {
    corsHandler {
      path("todo") {
        get {
          parameter('id.?) {
            case Some(id) => logger.info(s"Get todo with $id"); rejectEmptyResponse(complete(store.get(id)))
            case None =>     logger.info("Get all todo keys"); complete((StatusCodes.OK, store.keys))
          }
        } ~
        post {
          entity(as[String]) {
            newItem => logger.info(s"Add new item $newItem"); complete(store.add(newItem))
          }
        } ~
        put {
          entity(as[ToDoItem]) {
            item => logger.info(s"Update item $item"); complete(store.update(item))
          }
        } ~
        delete {
          parameter('id) {
            id => logger.info(s"Delete todo with $id"); rejectEmptyResponse(complete(store.delete(id)))
          }
        }
      } ~
      path("todo" / "full") {
        get {
          logger.info("Get all todos"); complete(store.getAll)
        }
      }
    }
  }
}


// credits to https://dzone.com/articles/handling-cors-in-akka-http
trait CORSHandler{
  private val corsResponseHeaders = List(
    `Access-Control-Allow-Origin`.*,
    `Access-Control-Allow-Credentials`(true),
    `Access-Control-Allow-Headers`("Authorization",
      "Content-Type", "X-Requested-With")
  )
  //this directive adds access control headers to normal responses
  private def addAccessControlHeaders(): Directive0 = {
    respondWithHeaders(corsResponseHeaders)
  }
  //this handles preflight OPTIONS requests.
  private def preflightRequestHandler: Route = options {
    complete(HttpResponse(StatusCodes.OK).
      withHeaders(`Access-Control-Allow-Methods`(OPTIONS, POST, PUT, GET, DELETE)))
  }
  // Wrap the Route with this method to enable adding of CORS headers
  def corsHandler(r: Route): Route = addAccessControlHeaders() {
    preflightRequestHandler ~ r
  }
  // Helper method to add CORS headers to HttpResponse
  // preventing duplication of CORS headers across code
  def addCORSHeaders(response: HttpResponse):HttpResponse =
    response.withHeaders(corsResponseHeaders)
}