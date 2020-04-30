package de.quinesoft.checklist

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import com.typesafe.scalalogging.Logger
import de.quinesoft.checklist.config.ChecklistConfig
import de.quinesoft.checklist.routes.Routing
import pureconfig.ConfigSource
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

  logger.info("Starting server")
  Http().bindAndHandle(new Routing(config).routes, config.host, config.port.number)
}
