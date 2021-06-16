package de.quinesoft.checklist.config

/** @author <a href="mailto:krickl@quinesoft.de>Maximilian Krickl</a>
  */

case class ChecklistConfig(host: String, port: Port, version: String, storage: StorageConfig)
case class Port(number: Int) extends AnyVal

case class StorageConfig(path: String, writeDelaySec: WriteDelaySec)
case class WriteDelaySec(number: Int) extends AnyVal
