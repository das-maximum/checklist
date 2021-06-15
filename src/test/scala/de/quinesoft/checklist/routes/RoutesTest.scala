package de.quinesoft.checklist.routes

import akka.actor
import akka.actor.testkit.typed.scaladsl.ActorTestKit
import akka.actor.typed.ActorSystem
import akka.http.scaladsl.model._
import akka.http.scaladsl.model.headers.RawHeader
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.testkit.ScalatestRouteTest
import de.quinesoft.checklist.model.ToDoItem
import de.quinesoft.checklist.persistence.ChecklistStore
import org.scalatest.BeforeAndAfterEach
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class RoutesTest extends AnyFlatSpec with Matchers with ScalaFutures with ScalatestRouteTest with BeforeAndAfterEach {

  lazy val testKit: ActorTestKit = ActorTestKit()
  implicit def typedSystem: ActorSystem[Nothing] = testKit.system

  override protected def createActorSystem(): actor.ActorSystem =
    testKit.system.classicSystem

  var storeStub: ChecklistStore = _
  var sut: Route = _

  override protected def beforeEach(): Unit = {
    storeStub = ChecklistStoreTestStub
    sut = new Routes(storeStub).routes
  }

  "Route (GET /api/todo)" should "return an empty list" in {
    val request = HttpRequest(uri = "/api/todo")

    request ~> addHeader(RawHeader(Endpoints.AUTH_HEADER, "123abc")) ~> sut ~> check {
      status shouldBe StatusCodes.OK
      contentType shouldBe ContentTypes.`application/json`

      entityAs[String] shouldBe """[]"""
    }
  }

  it should "return all items of a list" in {
    storeStub.add(ToDoItem.create("Milk").get)
    storeStub.add(ToDoItem.create("Cheese").get)
    storeStub.add(ToDoItem.create("Cucumber").get)
    storeStub.add(ToDoItem.create("Carrot").get)
    storeStub.add(ToDoItem.create("Eggs").get)
    val request = HttpRequest(uri = "/api/todo")

    request ~> addHeader(RawHeader(Endpoints.AUTH_HEADER, "123abc")) ~> sut ~> check {
      status shouldBe StatusCodes.OK
      contentType shouldBe ContentTypes.`application/json`

    }
  }
}

object ChecklistStoreTestStub extends ChecklistStore {
  var map: Map[String, ToDoItem] = Map.empty

  override def add(newItem: ToDoItem): Boolean =
    if (map.contains(newItem.id)) {
      false
    }
    else {
      map = map + (newItem.id -> newItem)
      false
    }

  override def update(changedItem: ToDoItem): Boolean =
    if (map.contains(changedItem.id)) {
      map = map + (changedItem.id -> changedItem)
      true
    }
    else {
      false
    }

  override def delete(id: String): Unit = map = map - id

  override def get(id: String): Option[ToDoItem] = map.get(id)

  override def getAll: List[ToDoItem] = map.values.toList

  override def keys: Set[String] = map.keySet
}
