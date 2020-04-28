package de.quinesoft.checklist

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import com.typesafe.scalalogging.Logger
import de.quinesoft.checklist.config.ChecklistConfig
import de.quinesoft.checklist.routes.Routing
import pureconfig.ConfigSource

/**
 * @author <a href="mailto:krickl@quinesoft.de>Maximilian Krickl</a>
 */
object Checklist extends App {
  private val logger: Logger = Logger(Checklist.getClass.getCanonicalName)
  private implicit val system: ActorSystem = ActorSystem("checklist")

  logger.info("Loading config")
  val config: ChecklistConfig = ConfigSource.file("src/main/resources/application.conf").loadOrThrow[ChecklistConfig]

  logger.info("Starting server")
  Http().bindAndHandle(Routing.routes, config.host, config.port)
}
