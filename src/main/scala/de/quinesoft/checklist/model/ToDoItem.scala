package de.quinesoft.checklist.model

import java.time.Instant
import java.util.UUID

/**
 * @author <a href="mailto:krickl@quinesoft.de>Maximilian Krickl</a>
 */
case class ToDoItem(id: String = UUID.randomUUID().toString, text: String, created: Instant = Instant.now(), done: Boolean = false)
