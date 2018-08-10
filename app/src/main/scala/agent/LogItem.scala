package agent

import io.circe._
import io.circe.syntax._

import agent.Event._


class LogItem(state: String, action: String, event: Event, score: Double) {

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
      ("energy", Json.fromDoubleOrNull(event.energy)),
      ("x", Json.fromInt(event.x)),
      ("y", Json.fromInt(event.y)),
      ("score", Json.fromDoubleOrNull(score))
    )
  }

}
