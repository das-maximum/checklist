package de.quinesoft.checklist.persistence

import java.nio.charset.StandardCharsets
import java.nio.file.{Files, Path, Paths, StandardOpenOption}
import java.util.concurrent.TimeUnit
import akka.actor.ActorSystem
import com.typesafe.scalalogging.Logger
import de.quinesoft.checklist.config.StorageConfig
import de.quinesoft.checklist.model.ToDoItem
import io.circe.parser._
import io.circe.syntax.EncoderOps

import scala.collection.immutable.Queue
import scala.concurrent.duration.FiniteDuration
import scala.concurrent.ExecutionContext
import scala.io.{BufferedSource, Codec, Source}

/** @author <a href="mailto:krickl@quinesoft.de>Maximilian Krickl</a>
  */
class MapStore(storage: StorageConfig)(implicit val ec: ExecutionContext, actor: ActorSystem)
    extends ChecklistStore
    with PersistentStore {

  private val logger = Logger(classOf[MapStore])

  private var cache: Map[String, ToDoItem]               = Map.empty
  private var persistingQueue: Queue[PersistingToDoItem] = Queue.empty

  private implicit val fileCodec: Codec = Codec.UTF8

  loadExistingItems()
  startPersisting()

  override def add(newItem: ToDoItem): Boolean =
    if (cache.contains(newItem.id)) {
      false
    } else {
      cache += (newItem.id -> newItem)
      persist(PersistingToDoItem(newItem.id, Some(newItem)))
      true
    }

  override def update(changedItem: ToDoItem): Boolean = {
    if (cache.contains(changedItem.id)) {
      cache = cache + (changedItem.id -> changedItem)
      persist(PersistingToDoItem(changedItem.id, Some(changedItem)))
      true
    } else {
      false
    }
  }

  override def get(id: String): Option[ToDoItem] =
    cache.get(id)

  override def getAll: Set[ToDoItem] = cache.values.toSet

  override def delete(id: String): Unit = {
    cache -= id
    persist(PersistingToDoItem(id, None))
    ()
  }

  override def keys: Set[String] =
    cache.keySet

  override def persist(item: PersistingToDoItem): Unit =
    persistingQueue = persistingQueue :+ item

  private def loadExistingItems(): Unit = {
    logger.info("Loading stored items")

    Files
      .newDirectoryStream(Paths.get(storage.path))
      .forEach(singleFile => {
        logger.debug(s"Reading in file ${singleFile.toString}")
        val source: BufferedSource = Source.fromFile(singleFile.toUri)
        parse(source.mkString) match {
          case Left(exception) => logger.warn(s"Could not parse $singleFile: $exception")
          case Right(json) =>
            json.as[ToDoItem] match {
              case Left(exception) =>
                logger.warn(s"Could not encode ToDoItem from $singleFile ($json): $exception")
              case Right(item) => cache += (item.id -> item)
            }
        }
        source.close()
      })
  }

  private def startPersisting(): Unit = actor.scheduler.scheduleAtFixedRate(
    initialDelay = FiniteDuration(0, TimeUnit.SECONDS),
    interval = FiniteDuration(storage.writeDelaySec.number, TimeUnit.SECONDS)
  ) { () =>
    writeToDisk()
  }

  private def writeToDisk(): Unit = {
    if (persistingQueue.nonEmpty) {
      val tmp = persistingQueue
      logger.info(s"Writing ${tmp.size} elements")

      tmp.foreach(item => {
        val path: Path = Paths.get(storage.path).resolve(item.id)
        item.todo match {
          case Some(value) =>
            logger.debug(s"Writing $path")
            Files.writeString(
              path,
              value.asJson.noSpaces,
              StandardCharsets.UTF_8,
              StandardOpenOption.CREATE,
              StandardOpenOption.WRITE,
              StandardOpenOption.TRUNCATE_EXISTING
            )
          case None =>
            logger.debug(s"Deleting item ${item.id} @ $path")
            Files.deleteIfExists(path)
        }
      })

      persistingQueue = persistingQueue.diff(tmp)
    }
  }
}
