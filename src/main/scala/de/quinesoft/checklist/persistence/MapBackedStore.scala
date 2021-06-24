package de.quinesoft.checklist.persistence

import akka.actor.ActorSystem
import com.typesafe.scalalogging.Logger
import de.quinesoft.checklist.config.StorageConfig
import io.circe.Codec
import io.circe.syntax._

import java.nio.charset.StandardCharsets
import java.nio.file.{Files, Path, Paths, StandardOpenOption}
import java.util.concurrent.TimeUnit
import scala.collection.immutable.Queue
import scala.concurrent.ExecutionContext
import scala.concurrent.duration.FiniteDuration
import scala.io.{BufferedSource, Source}
import io.circe.parser._

trait StoreableItem {
  type Id = String

  def id: Id
}

class MapBackedStore[T <: StoreableItem](storage: StorageConfig)(implicit
    val ec: ExecutionContext,
    actor: ActorSystem,
    codec: Codec[T]
) {
  type Id = String

  private val logger = Logger(classOf[MapStore])

  private var cache: Map[Id, T]         = Map.empty
  private var persistingQueue: Queue[T] = Queue.empty
  private var deleteQueue: Queue[Id]    = Queue.empty

  loadExistingItems()
  startPersisting()

  def +(newItem: T): Unit = {
    cache += (newItem.id -> newItem)
    persistingQueue = persistingQueue :+ newItem
  }

  def -(deleteItem: T): Unit = {
    cache -= deleteItem.id
    deleteQueue = deleteQueue :+ deleteItem.id
  }

  def getAll: List[T] = cache.values.toList

  def get(id: Id): Option[T] = cache.get(id)

  def keys: Set[Id] = cache.keySet

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
            json.as[T] match {
              case Left(exception) =>
                logger.warn(s"Could not decode item from $singleFile ($json): $exception")
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
      val temp = persistingQueue
      logger.info(s"Writing ${temp.size} elements")

      temp.foreach(item => {
        val path: Path = Paths.get(storage.path).resolve(item.id)

        logger.debug(s"Writing $path")
        Files.writeString(
          path,
          item.asJson.noSpaces,
          StandardCharsets.UTF_8,
          StandardOpenOption.CREATE,
          StandardOpenOption.WRITE,
          StandardOpenOption.TRUNCATE_EXISTING
        )
      })

      persistingQueue = persistingQueue.diff(temp)
    }

    if (deleteQueue.nonEmpty) {
      val temp = deleteQueue
      logger.info(s"Deleting ${temp.size} elements")

      temp.foreach(id => {
        val path: Path = Paths.get(storage.path).resolve(id)

        logger.info(s"Deleting item $id @ $path")
        Files.delete(path)
      })

      deleteQueue = deleteQueue.diff(temp)
    }
  }
}
