package de.quinesoft.checklist.routes

import akka.actor
import akka.actor.testkit.typed.scaladsl.ActorTestKit
import akka.actor.typed.ActorSystem
import akka.http.scaladsl.model._
import akka.http.scaladsl.model.headers.RawHeader
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.testkit.ScalatestRouteTest
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport
import de.quinesoft.checklist.model.ToDoItem
import de.quinesoft.checklist.persistence.ChecklistStore
import org.scalatest.BeforeAndAfterEach
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import java.util.UUID

class RoutesTest
    extends AnyFlatSpec
    with Matchers
    with ScalaFutures
    with ScalatestRouteTest
    with BeforeAndAfterEach
    with FailFastCirceSupport {

  import ToDoItem._

  lazy val testKit: ActorTestKit                 = ActorTestKit()
  implicit def typedSystem: ActorSystem[Nothing] = testKit.system

  override protected def createActorSystem(): actor.ActorSystem =
    testKit.system.classicSystem

  val authHeader: List[HttpHeader] = List(RawHeader(Endpoints.AUTH_HEADER, "123abc"))

  var storeStub: ChecklistStore = _
  var sut: Route                = _

  override protected def beforeEach(): Unit = {
    storeStub = new ChecklistStoreTestStub
    sut = new Routes(storeStub).routes
  }

  "Route (GET /api/todo)" should "return an empty list" in {
    val request = HttpRequest(uri = "/api/todo", headers = authHeader)

    request ~> sut ~> check {
      status shouldBe StatusCodes.OK
      contentType shouldBe ContentTypes.`application/json`

      entityAs[List[ToDoItem]] shouldBe List.empty
    }
  }

  it should "return all ids" in {
    val inputFixtures: List[ToDoItem] = List(
      ToDoItem.create("Milk").get,
      ToDoItem.create("Cheese").get,
      ToDoItem.create("Cucumber").get,
      ToDoItem.create("Carrot").get,
      ToDoItem.create("Eggs").get
    )

    inputFixtures.foreach(storeStub.add)

    val onlyIds = inputFixtures.map(_.id)

    val request = HttpRequest(uri = "/api/todo", headers = authHeader)

    request ~> sut ~> check {
      status shouldBe StatusCodes.OK
      contentType shouldBe ContentTypes.`application/json`

      entityAs[Set[String]] should contain theSameElementsAs onlyIds
    }
  }

  "Route (GET /api/todo/full)" should "return all full items" in {
    val inputFixtures: List[ToDoItem] = List(
      ToDoItem.create("Milk").get,
      ToDoItem.create("Cheese").get,
      ToDoItem.create("Cucumber").get,
      ToDoItem.create("Carrot").get,
      ToDoItem.create("Eggs").get
    )

    inputFixtures.foreach(storeStub.add)

    val request = HttpRequest(uri = "/api/todo/full", headers = authHeader)

    request ~> sut ~> check {
      status shouldBe StatusCodes.OK
      contentType shouldBe ContentTypes.`application/json`

      entityAs[Set[ToDoItem]] should contain theSameElementsAs inputFixtures
    }
  }

  "Route (GET /api/todo/{id})" should "return the specific item" in {
    val cucumber: ToDoItem = ToDoItem.create("Cucumber").get
    val inputFixtures: List[ToDoItem] = List(
      ToDoItem.create("Milk").get,
      ToDoItem.create("Cheese").get,
      cucumber,
      ToDoItem.create("Carrot").get,
      ToDoItem.create("Eggs").get
    )

    inputFixtures.foreach(storeStub.add)

    val request = HttpRequest(uri = s"/api/todo/${cucumber.id}", headers = authHeader)

    request ~> sut ~> check {
      status shouldBe StatusCodes.OK
      contentType shouldBe ContentTypes.`application/json`

      entityAs[ToDoItem] shouldBe cucumber
    }
  }

  "Route (POST /api/todo)" should "add an item" in {
    storeStub.keys.size shouldBe 0

    val todoBody = HttpEntity(ContentTypes.`text/plain(UTF-8)`, "Cucumber")
    val request = HttpRequest(
      uri = "/api/todo",
      method = HttpMethods.POST,
      headers = authHeader,
      entity = todoBody
    )

    request ~> sut ~> check {
      status shouldBe StatusCodes.OK
      storeStub.keys.size shouldBe 1

      entityAs[ToDoItem] shouldBe storeStub.getAll.head
    }
  }

  "Route (PUT /api/todo/{id})" should "update an item" in {
    val cucumber = ToDoItem.create("Cucumber").get
    storeStub.add(cucumber)

    val gurkin = cucumber.copy(text = "Gurkin")

    import io.circe.syntax._

    val todoBody = HttpEntity(ContentTypes.`application/json`, gurkin.asJson.toString)
    val request = HttpRequest(
      uri = s"/api/todo/${cucumber.id}",
      method = HttpMethods.PUT,
      headers = authHeader,
      entity = todoBody
    )

    request ~> sut ~> check {
      status shouldBe StatusCodes.OK
      storeStub.get(cucumber.id).get shouldBe gurkin
    }
  }

  it should "fail on unknown id" in {
    val cucumber = ToDoItem.create("Cucumber").get
    storeStub.add(cucumber)

    import io.circe.syntax._

    val todoBody = HttpEntity(
      ContentTypes.`application/json`,
      cucumber.copy(id = cucumber.id.replaceAll("-", ".")).asJson.toString
    )
    val request = HttpRequest(
      uri = s"/api/todo/${cucumber.id}",
      method = HttpMethods.PUT,
      headers = authHeader,
      entity = todoBody
    )

    request ~> sut ~> check {
      status shouldBe StatusCodes.BadRequest
    }
  }

  "Route (DELETE /api/todo/{id})" should "delete an item" in {
    val cucumber = ToDoItem.create("Cucumber").get
    storeStub.add(cucumber)

    storeStub.keys.size shouldBe 1

    val request = HttpRequest(
      uri = s"/api/todo/${cucumber.id}",
      method = HttpMethods.DELETE,
      headers = authHeader
    )

    request ~> sut ~> check {
      status shouldBe StatusCodes.OK
      storeStub.keys.size shouldBe 0
    }
  }

  it should "not delete an item if not in store" in {
    val cucumber = ToDoItem.create("Cucumber").get
    storeStub.add(cucumber)

    storeStub.keys.size shouldBe 1

    val request = HttpRequest(
      uri = s"/api/todo/${UUID.randomUUID()}",
      method = HttpMethods.DELETE,
      headers = authHeader
    )

    request ~> sut ~> check {
      status shouldBe StatusCodes.OK
      storeStub.keys.size shouldBe 1
    }
  }
}

class ChecklistStoreTestStub extends ChecklistStore {
  var map: Map[String, ToDoItem] = Map.empty

  override def add(newItem: ToDoItem): Boolean =
    if (map.contains(newItem.id)) {
      false
    } else {
      map = map + (newItem.id -> newItem)
      true
    }

  override def update(changedItem: ToDoItem): Boolean =
    if (map.contains(changedItem.id)) {
      map = map + (changedItem.id -> changedItem)
      true
    } else {
      false
    }

  override def delete(id: String): Unit = map = map - id

  override def get(id: String): Option[ToDoItem] = map.get(id)

  override def getAll: Set[ToDoItem] = map.values.toSet

  override def keys: Set[String] = map.keySet
}
