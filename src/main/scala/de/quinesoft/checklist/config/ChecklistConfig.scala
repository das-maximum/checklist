package de.quinesoft.checklist.config

import pureconfig.ConfigReader

/**
 * @author <a href="mailto:krickl@quinesoft.de>Maximilian Krickl</a>
 */
case class ChecklistConfig(host: String, port: Int)

object ChecklistConfig {
  implicit val configReader: ConfigReader[ChecklistConfig] = pureconfig.module.magnolia.auto.reader.exportReader
}