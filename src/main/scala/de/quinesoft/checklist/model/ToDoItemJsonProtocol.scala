package de.quinesoft.checklist.model

import java.time.Instant
import java.time.format.DateTimeParseException

import spray.json.{DefaultJsonProtocol, DeserializationException, JsString, JsValue, RootJsonFormat}

/**
 * @author <a href="mailto:krickl@quinesoft.de>Maximilian Krickl</a>
 */
class ToDoItemJsonProtocol extends DefaultJsonProtocol {

  implicit val instantProtocol: RootJsonFormat[Instant] = new RootJsonFormat[Instant] {
    override def read(json: JsValue): Instant = json match {
      case JsString(value) =>
        try {
          Instant.parse(value)
        } catch {
          case _: DateTimeParseException =>
            throw DeserializationException(s"Timestamp has wrong format. Must be like this: 2020-03-31T21:10:43Z. Found $s")
        }

      case _ =>
        throw DeserializationException(s"Timestamp is no string. Found ${json.prettyPrint}")
    }

    override def write(obj: Instant): JsValue = JsString(obj.toString)
  }

  implicit val toDoItemJsonProtocol: RootJsonFormat[ToDoItem] = rootFormat(jsonFormat4(ToDoItem.apply))
}
