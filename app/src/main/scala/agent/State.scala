package agent

import io.circe._
import io.circe.syntax._


class AgentState(
  val health: Double,
  val energyLevel: Double,
  // val clock: Double,
  val elementStates: List[ElementState]
) {
  def elementTypes: List[String] = elementStates.map(_.elementType).toList
  def totalPercentKnown: Double = elementStates.map(_.percentKnown).foldLeft(0.0)(_ + _)
  def toJson(): Json = Json.obj(
    ("health", Json.fromDoubleOrNull(health)),
    ("energyLevel", Json.fromDoubleOrNull(energyLevel)),
    // ("clock", Json.fromDoubleOrNull(clock)),
    ("elementStates", Json.fromValues(elementStates.map(_.toJson())))
  )
  def getElementState(elementType: String): Option[ElementState] = {
    for (es <- elementStates) if (es.elementType == elementType) return Some(es)
    return None
  }
}

class ElementState(
  val elementType: String,
  val indicator: Boolean,
  val hazard: Boolean,
  val value: Option[Double],
  val northQuadrant: QuadrantState,
  val southQuadrant: QuadrantState,
  val westQuadrant: QuadrantState,
  val eastQuadrant: QuadrantState
) {
  def percentKnown: Double = (northQuadrant.percentKnown + southQuadrant.percentKnown + westQuadrant.percentKnown + eastQuadrant.percentKnown) / 4
  def toJson(): Json = Json.obj(
    ("elementType", Json.fromString(elementType)),
    ("indicator", Json.fromBoolean(indicator)),
    ("hazard", Json.fromBoolean(hazard)),
    ("value", Json.fromDoubleOrNull(value.getOrElse(Double.NaN))),
    ("northQuadrant", northQuadrant.toJson()),
    ("southQuadrant", southQuadrant.toJson()),
    ("westQuadrant", westQuadrant.toJson()),
    ("eastQuadrant", eastQuadrant.toJson())
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
