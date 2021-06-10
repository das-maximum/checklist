package de.quinesoft.checklist.persistence

import java.nio.file.{Files, Path}

import akka.actor.ActorSystem
import de.quinesoft.checklist.config.{StorageConfig, WriteDelaySec}
import de.quinesoft.checklist.model.ToDoItem
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class MapStoreTest extends AnyFlatSpec with Matchers {
  private val storagePath: Path = Files.createTempDirectory("scalatest_mapstore")

  val config: StorageConfig = StorageConfig(storagePath.toString, WriteDelaySec(1))

  implicit val actor: ActorSystem = ActorSystem("TestActor")
  import scala.concurrent.ExecutionContext.Implicits.global
  val sut = new MapStore(config)

  "add" should "add a new item to the store" in {
    sut.add("Milk") match {
      case Some(item) => item.text shouldBe "Milk"
      case None => fail("There should have been a new item")
    }
  }

  it should "not add a new item with no name" in {
    sut.add("  ") match {
      case Some(item) => fail(s"there should have been no $item")
      case None => succeed
    }
  }

  "update" should "update an existing item" in {
    sut.add("Milk") match {
      case Some(value) =>
        val update = value.copy(text = "Molk")
        sut.update(update) shouldBe true
      case None => fail("there should have been an item")
    }
  }

  it should "not allow updates of unknown items" in {
    val item = ToDoItem(text = "Mjölk")
    sut.update(item) shouldBe false
  }

  "delete" should "remove an item" in {
    sut.add("Cheese") match {
      case Some(item) =>
        val id = item.id
        sut.delete(id)
        sut.get(id) match {
          case Some(_) => fail("There should not be an item")
          case None => succeed
        }
      case None => fail("Cheese was not added to the list")
    }
  }
}
