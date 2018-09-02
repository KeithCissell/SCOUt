package agent

import io.circe._
import io.circe.syntax._


class AgentState(
  val health: Double,
  val energyLevel: Double,
  // val clock: Double,
  val elementStates: List[ElementState]
) {
  def toJson(): Json = Json.obj(
    ("health", Json.fromDoubleOrNull(health)),
    ("energyLevel", Json.fromDoubleOrNull(energyLevel)),
    // ("clock", Json.fromDoubleOrNull(clock)),
    ("elementStates", Json.fromValues(elementStates.map(_.toJson())))
  )
}

class ElementState(
  val elementType: String,
  val value: Option[Double],
  val northQuadrant: QuadrantState,
  val southQuadrant: QuadrantState,
  val eastQuadrant: QuadrantState,
  val westQuadrant: QuadrantState
) {
  def toJson(): Json = Json.obj(
    ("elementType", Json.fromString(elementType)),
    ("value", Json.fromDoubleOrNull(value.getOrElse(Double.NaN))),
    ("northQuadrant", northQuadrant.toJson()),
    ("southQuadrant", southQuadrant.toJson()),
    ("eastQuadrant", eastQuadrant.toJson()),
    ("westQuadrant", westQuadrant.toJson())
  )
}

class QuadrantState(
  val percentKnown: Double,
  val averageValue: Option[Double],
  val immediateValue: Option[Double],
) {
  def toJson(): Json = Json.obj(
    ("percentKnown", Json.fromDoubleOrNull(percentKnown)),
    ("averageValue", Json.fromDoubleOrNull(averageValue.getOrElse(Double.NaN))),
    ("immediateValue", Json.fromDoubleOrNull(immediateValue.getOrElse(Double.NaN)))
  )
}
