package de.quinesoft.checklist.persistence

import java.nio.file.{Files, Path}

import akka.Done
import akka.actor.ActorSystem
import de.quinesoft.checklist.config.{StorageConfig, WriteDelaySec}
import de.quinesoft.checklist.model.ToDoItem
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import scala.concurrent.Future
import scala.util.{Failure, Success}

class MapStoreTest extends AnyFlatSpec with Matchers {
  private val storagePath: Path = Files.createTempDirectory("scalatest_mapstore")

  val config: StorageConfig = StorageConfig(storagePath.toString, WriteDelaySec(1))

  implicit val actor: ActorSystem = ActorSystem("TestActor")
  import scala.concurrent.ExecutionContext.Implicits.global
  val sut = new MapStore(config)

  "add" should "add a new item to the store" in {
    sut.add("Milk").onComplete {
      case Failure(exception) => fail(exception)
      case Success(value) => value match {
        case Some(item) => item.text shouldBe "Milk"
        case None => fail("There should have been a new item")
      }
    }
  }

  it should "not add a new item with no name" in {
    sut.add("  ").onComplete {
      case Failure(exception) => fail(exception)
      case Success(value) => value match {
        case Some(item) => fail(s"there should have been no $item")
        case None => succeed
      }
    }
  }

  "update" should "update an existing item" in {
    sut.add("Milk").onComplete {
      case Failure(exception) => fail(exception)
      case Success(value) => value match {
        case Some(value) =>
          val update = value.copy(text = "Molk")
          sut.update(update).onComplete {
            case Failure(exception) => fail(exception)
            case Success(_) => succeed
            }
        case None => fail("there should have been an item")
      }
    }
  }

  it should "not allow updates of unknown items" in {
    val item = ToDoItem(text = "Mjölk")
    sut.update(item).onComplete {
      case Failure(_) => succeed
      case Success(_) => fail("Update should not be possible")
    }
  }

  "delete" should "remove an item" in {
    sut.add("Cheese").onComplete {
      case Failure(exception) => fail(exception)
      case Success(value) => value match {
        case Some(item) =>
          val id = item.id
          sut.delete(id).onComplete {
            case Failure(exception) => fail(exception)
            case Success(_) => sut.get(id).onComplete {
              case Failure(exception) => fail(exception)
              case Success(value) => value match {
                case Some(_) => fail("There should not be an item")
                case None => succeed
              }
            }
          }
        case None => fail("Cheese was not added to the list")
      }
    }
  }
}
