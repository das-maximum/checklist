package de.quinesoft.checklist.routes

import de.quinesoft.checklist.model.ToDoItem
import sttp.tapir._
import sttp.tapir.generic.auto._
import sttp.tapir.json.circe._

import java.util.UUID

/**
 * Collection of all HTTP REST endpoints.
 *
 * @author <a href="mailto:krickl@quinesoft.de>Maximilian Krickl</a>
 */
object Endpoints {
  type AuthToken = String
  val AUTH_HEADER: AuthToken = "X-Auth-Token"

  private val basePath: Endpoint[Unit, Unit, Unit, Any] = endpoint
    .in("api" / "todo")

  val getIds: Endpoint[AuthToken, Unit, Set[String], Any] = basePath
    .get
    .in(header[AuthToken](AUTH_HEADER))
    .out(jsonBody[Set[String]])

  val getItem: Endpoint[(AuthToken, UUID), String, ToDoItem, Any] = basePath
    .get
    .in(header[AuthToken](AUTH_HEADER))
    .in(path[UUID]("id").description("Id of the todo item"))
    .errorOut(stringBody)
    .out(jsonBody[ToDoItem])

  val newItem: Endpoint[(AuthToken, String), String, ToDoItem, Any] = basePath
    .post
    .in(header[AuthToken](AUTH_HEADER))
    .in(stringBody)
    .errorOut(stringBody)
    .out(jsonBody[ToDoItem])

  val updateItem: Endpoint[(AuthToken, UUID, ToDoItem), String, String, Any] = basePath
    .put
    .in(header[AuthToken](AUTH_HEADER))
    .in(path[UUID]("id"))
    .in(jsonBody[ToDoItem])
    .out(stringBody)
    .errorOut(stringBody)

  val deleteItem: Endpoint[(AuthToken, UUID), String, String, Any] = basePath
    .delete
    .in(header[AuthToken](AUTH_HEADER))
    .in(path[UUID]("id"))
    .errorOut(stringBody)
    .out(stringBody)

  val getAllTodos: Endpoint[AuthToken, Unit, Set[ToDoItem], Any] = basePath
    .get
    .in("full")
    .in(header[AuthToken](AUTH_HEADER))
    .out(jsonBody[Set[ToDoItem]])

  val version: Endpoint[Unit, Unit, String, Any] = endpoint
    .get
    .in("api" / "version")
    .out(stringBody)
}
