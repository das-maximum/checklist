package de.quinesoft.checklist

import java.io.BufferedWriter
import java.lang.management.ManagementFactory
import java.nio.charset.StandardCharsets
import java.nio.file.{Files, Path, Paths, StandardOpenOption}
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import com.typesafe.scalalogging.Logger
import de.quinesoft.checklist.config.ChecklistConfig
import de.quinesoft.checklist.persistence.MapStore
import de.quinesoft.checklist.routes.Routes
import pureconfig.ConfigSource
import pureconfig.generic.auto._

import scala.concurrent.ExecutionContext

/**
 * @author <a href="mailto:krickl@quinesoft.de>Maximilian Krickl</a>
 */
object Checklist extends App {
  private val logger: Logger = Logger(Checklist.getClass.getCanonicalName)
  private implicit val ec: ExecutionContext = ExecutionContext.Implicits.global
  private implicit val system: ActorSystem = ActorSystem("checklist")

  logger.info("Loading config")
  val config: ChecklistConfig = ConfigSource.default.loadOrThrow[ChecklistConfig]

  logger.info("Connect to storage")
  val store: MapStore = new MapStore(config.storage)

  logger.debug("Write pid file")
  writePidFile()

  logger.info("Starting server")
  Http()
    .newServerAt(config.host, config.port.number)
    .bind(new Routes(store).routes)

  private def writePidFile(): Unit = {
    val pidPath: Path = Paths.get("./checklist.pid")
    val pid: String = ManagementFactory.getRuntimeMXBean.getName.split('@')(0)
    val writer: BufferedWriter = Files.newBufferedWriter(pidPath, StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)
    writer.write(pid)
    writer.close()
  }
}
