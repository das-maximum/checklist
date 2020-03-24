package de.quinesoft.checklist.model

import java.time.Instant
import java.util.UUID

import de.quinesoft.checklist.persistence.Store.Id

/**
 * @author <a href="mailto:krickl@quinesoft.de>Maximilian Krickl</a>
 */
case class ToDoItem(id: Id = UUID.randomUUID().toString, text: String, created: Instant = Instant.now(), done: Boolean = false)
