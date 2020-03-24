package de.quinesoft.checklist.model

import spray.json.DefaultJsonProtocol
import spray.json._

/**
 * @author <a href="mailto:krickl@quinesoft.de>Maximilian Krickl</a>
 */
class ToDoItemJsonProtocol extends DefaultJsoProtocol {

  val toDoItemJsonProtocol = rootFormat(jsonFormat4(ToDoItem.apply))
}
