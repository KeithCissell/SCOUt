package agent

import io.circe._
import io.circe.syntax._
import scoututil.Util._


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
  def roundOff(fps: Int): AgentState = {
    val h = roundDoubleX(health, fps)
    val el = roundDoubleX(energyLevel, fps)
    val es = for (e <- elementStates) yield e.roundOff(fps)
    return new AgentState(h, el, es)
  }
}

class ElementState(
  val elementType: String,
  val indicator: Boolean,
  val hazard: Boolean,
  val percentKnownInRange: Double,
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
    ("percentKnownInRange", Json.fromDoubleOrNull(percentKnownInRange)),
    ("value", Json.fromDoubleOrNull(value.getOrElse(Double.NaN))),
    ("north", northQuadrant.toJson()),
    ("south", southQuadrant.toJson()),
    ("west", westQuadrant.toJson()),
    ("east", eastQuadrant.toJson())
  )
  def getQuadrantState(q: String): QuadrantState = q match {
    case "north" => northQuadrant
    case "south" => southQuadrant
    case "west" => westQuadrant
    case "east" => eastQuadrant
  }
  def roundOff(fps: Int): ElementState = {
    val pkir = roundDoubleX(percentKnownInRange, fps)
    val v = roundDoubleX(value, fps)
    val nq = northQuadrant.roundOff(fps)
    val sq = southQuadrant.roundOff(fps)
    val wq = westQuadrant.roundOff(fps)
    val eq = eastQuadrant.roundOff(fps)
    return new ElementState(elementType, indicator, hazard, pkir, v, nq, sq, wq, eq)
  }
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
  def roundOff(fps: Int): QuadrantState = {
    val pk = roundDoubleX(percentKnown, fps)
    val av = roundDoubleX(averageValue, fps)
    val iv = roundDoubleX(immediateValue, fps)
    return new QuadrantState(pk, av, iv)
  }
}
