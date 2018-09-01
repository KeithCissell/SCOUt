package agent

import io.circe._
import io.circe.syntax._

import agent.Event._


class LogItemShort(val state: AgentState, val action: String, val event: Event, val shortTermScore: Double) {

  def toJson(): Json = {
    val outcome = event match {
      case event: Successful => "successful"
      case event: Unsuccessful => "unsuccessful"
      case event: Fatal => "fatal"
    }
    return Json.obj(
      ("message", Json.fromString(event.msg)),
      ("action", Json.fromString(action)),
      ("outcome", Json.fromString(outcome)),
      ("timeStamp", Json.fromDoubleOrNull(event.timeStamp)),
      ("health", Json.fromDoubleOrNull(event.health)),
      ("energyLevel", Json.fromDoubleOrNull(event.energyLevel)),
      ("x", Json.fromInt(event.x)),
      ("y", Json.fromInt(event.y)),
      ("shortTermScore", Json.fromDoubleOrNull(shortTermScore))
    )
  }

}

class LogItem(val state: AgentState, val action: String, val event: Event, val shortTermScore: Double, val longTermScore: Double) {

  def toJson(): Json = {
    val outcome = event match {
      case event: Successful => "successful"
      case event: Unsuccessful => "unsuccessful"
      case event: Fatal => "fatal"
    }
    return Json.obj(
      ("message", Json.fromString(event.msg)),
      ("action", Json.fromString(action)),
      ("outcome", Json.fromString(outcome)),
      ("timeStamp", Json.fromDoubleOrNull(event.timeStamp)),
      ("health", Json.fromDoubleOrNull(event.health)),
      ("energyLevel", Json.fromDoubleOrNull(event.energyLevel)),
      ("x", Json.fromInt(event.x)),
      ("y", Json.fromInt(event.y)),
      ("shortTermScore", Json.fromDoubleOrNull(shortTermScore)),
      ("longTermScore", Json.fromDoubleOrNull(longTermScore))
    )
  }

}
