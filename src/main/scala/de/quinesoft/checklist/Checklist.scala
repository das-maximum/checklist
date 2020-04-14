package de.quinesoft.checklist

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import de.quinesoft.checklist.config.ChecklistConfig
import de.quinesoft.checklist.routes.Routing
import pureconfig.ConfigSource

/**
 * @author <a href="mailto:krickl@quinesoft.de>Maximilian Krickl</a>
 */
object Checklist extends App {
  implicit val system: ActorSystem = ActorSystem("checklist")

  val config: ChecklistConfig = ConfigSource.file("src/main/resources/application.conf").loadOrThrow[ChecklistConfig]

  Http().bindAndHandle(Routing.routes, config.host, config.port)
}
