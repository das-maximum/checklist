package de.quinesoft.checklist.persistence

import java.nio.charset.StandardCharsets
import java.nio.file.{Files, Path, Paths, StandardOpenOption}
import java.util.concurrent.TimeUnit

import akka.Done
import akka.actor.ActorSystem
import com.typesafe.scalalogging.Logger
import de.quinesoft.checklist.config.ChecklistConfig
import de.quinesoft.checklist.model.ToDoItem

import scala.collection.immutable.Queue
import scala.concurrent.duration.FiniteDuration
import scala.concurrent.{ExecutionContext, Future}
import scala.io.{Codec, Source}

/**
 * @author <a href="mailto:krickl@quinesoft.de>Maximilian Krickl</a>
 */
class MapStore(config: ChecklistConfig)(implicit val ec: ExecutionContext, actor: ActorSystem) extends ChecklistStore with PersistentStore {

  private val logger = Logger(classOf[MapStore])

  private var cache: Map[String, ToDoItem] = Map.empty
  private var persistingQueue: Queue[PersistingToDoItem] = Queue.empty

  loadExistingItems()
  startPersisting()

  override def update(changedItem: ToDoItem): Future[Done] = {
    cache = cache + (changedItem.id -> changedItem)
    Future(
      persist(PersistingToDoItem(changedItem.id, Some(changedItem)))
    )
    Future.successful(Done)
  }

  override def add(newItem: String): Future[Option[ToDoItem]] = newItem.trim match {
    case "" => Future.successful(None)
    case _ =>
      val newToDo = ToDoItem(text = newItem)
      cache += (newToDo.id -> newToDo)
      Future(
        persist(PersistingToDoItem(newToDo.id, Some(newToDo)))
      )
      Future.successful(Some(newToDo))
  }

  override def get(id: String): Future[Option[ToDoItem]] =
    Future.successful(cache.get(id))

  override def getAll: Future[List[ToDoItem]] = Future.successful(cache.values.toList)

  override def delete(id: String): Future[Done] = {
    cache -= id
    Future(
      persist(PersistingToDoItem(id, None))
    )
    Future.successful(Done)
  }

  override def keys: Future[Set[String]] =
    Future.successful(cache.keySet)

  override def persist(item: PersistingToDoItem): Unit =
    persistingQueue = persistingQueue :+ item

  private def loadExistingItems(): Unit = {
    import de.quinesoft.checklist.model.ToDoItemJsonProtocol._
    import spray.json._
    Files.newDirectoryStream(Paths.get(config.storage.path)).forEach(
      singleFile => {
        logger.info(s"Reading in file ${singleFile.toString}")
        val item = Source.fromFile(singleFile.toUri)(Codec.UTF8).mkString.parseJson.convertTo[ToDoItem]
        cache += (item.id -> item)
      }
    )
  }

  private def startPersisting(): Unit = actor.scheduler.scheduleAtFixedRate(initialDelay = FiniteDuration(0, TimeUnit.SECONDS), interval = FiniteDuration(config.storage.writeDelaySec.number, TimeUnit.SECONDS)) {
    () => writeToDisk()
  }

  private def writeToDisk(): Unit = {
    val tmp = persistingQueue
    logger.info(s"Writing ${tmp.size} elements")

    tmp.foreach(item => {
      val path: Path = Paths.get(config.storage.path).resolve(item.id)
      item.todo match {
        case Some(value) =>
          import spray.json._
          import de.quinesoft.checklist.model.ToDoItemJsonProtocol._

          logger.info(s"Writing $path")
          Files.writeString(
            path,
            value.toJson.compactPrint,
            StandardCharsets.UTF_8,
            StandardOpenOption.CREATE,
            StandardOpenOption.WRITE,
            StandardOpenOption.TRUNCATE_EXISTING)
        case None =>
          logger.info(s"Deleting item ${item.id} @ $path")
          Files.deleteIfExists(path)
      }
    })

    persistingQueue = persistingQueue.diff(tmp)
  }
}
