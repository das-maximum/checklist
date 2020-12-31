package de.quinesoft.checklist.persistence

import java.nio.file.{Files, Path}
import akka.actor.ActorSystem
import de.quinesoft.checklist.config.{StorageConfig, WriteDelaySec}
import de.quinesoft.checklist.model.ToDoItem
import org.scalatest.BeforeAndAfterEach
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class MapStoreTest extends AnyFlatSpec with Matchers with BeforeAndAfterEach {
  private val storagePath: Path = Files.createTempDirectory("scalatest_mapstore")

  val config: StorageConfig = StorageConfig(storagePath.toString, WriteDelaySec(1))

  implicit val actor: ActorSystem = ActorSystem("TestActor")
  import scala.concurrent.ExecutionContext.Implicits.global

  var sut: ChecklistStore = _

  override def beforeEach(): Unit = {
    sut = new MapStore(config)
  }

  "add" should "add a new item to the store" in {
    val newItem = ToDoItem.create("Milk").get

    sut.get(newItem.id) shouldBe None
    sut.add(newItem) shouldBe true
    sut.get(newItem.id) shouldBe Some(newItem)
  }

  it should "not add a new item twice" in {
    val newItem = ToDoItem.create("Milk").get
    sut.get(newItem.id) shouldBe None
    sut.add(newItem) shouldBe true
    sut.get(newItem.id) shouldBe Some(newItem)
    sut.add(newItem) shouldBe false
    sut.get(newItem.id) shouldBe Some(newItem)
  }

  "update" should "update an existing item" in {
    val newItem = ToDoItem.create("Milk").get
    sut.add(newItem) shouldBe true

    val changedItem = newItem.copy(text = "Molk")
    sut.update(changedItem) shouldBe true
  }

  it should "not allow updates of unknown items" in {
    val item = ToDoItem.create("Milk").get
    sut.update(item) shouldBe false
  }

  "delete" should "remove an item" in {
    val newItem = ToDoItem.create("Milk").get
    sut.add(newItem) shouldBe true
    sut.get(newItem.id) shouldBe Some(newItem)
    sut.delete(newItem.id)
    sut.get(newItem.id) shouldBe None
  }

  "getAll" should "respond with all items" in {
    val allItems: Future[Seq[ToDoItem]] = for {
      _                       <- sut.add("Cheese")
      _                       <- sut.add("Milk")
      _                       <- sut.add("Bread")
      allItems: Seq[ToDoItem] <- sut.getAll
    } yield allItems

    allItems.onComplete {
      case Failure(exception) => fail(exception)
      case Success(value) =>
        value.size shouldBe 3
        value should contain allElementsOf List("Cheese", "Milk", "Bread")
    }
  }

  "keys" should "result the same number of elements" in {
    val keys: Future[Set[String]] = for {
      _                 <- sut.add("Cheese")
      _                 <- sut.add("Milk")
      _                 <- sut.add("Bread")
      keys: Set[String] <- sut.keys
    } yield keys

    keys.onComplete {
      case Failure(exception) => fail(exception)
      case Success(value) =>
        value.size shouldBe 3
    }
  }
}
