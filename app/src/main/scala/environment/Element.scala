package environment.element

import scoututil.Util._

import scala.math.BigDecimal

// List of all elementTypes
// "Element Type" -> required?
object ElementTypes {
  val elementTypes = Map(
    "Elevation" -> true,
    "Latitude" -> true,
    "Longitude" -> true,
    "Decibel" -> false,
    "Temperature" -> false,
    "Wind Direction" -> false,
    "Wind Speed" -> false
  )
}

trait Element {
  var value: Option[Double]
  val name: String
  val unit: String
  val constant: Boolean
  val circular: Boolean
  val lowerBound: Double
  val upperBound: Double

  override def toString: String = value match {
    case Some(v)  => v.toString + " " + unit
    case None     => "NONE"
  }

  // Determines if element can be set
  def settable: Boolean = constant match {
    case true   => value == None
    case false  => true
  }
  // Set value
  def set(d: Double): Unit = {
    if (settable && circular) d match {
      case v if v < lowerBound  => set(v + (upperBound - lowerBound))
      case v if v > upperBound  => set(v - (upperBound - lowerBound))
      case _                    => value = Some(d)
    }
    if (settable && !circular) d match {
      case v if v < lowerBound  => value = Some(lowerBound)
      case v if v > upperBound  => value = Some(upperBound)
      case _                    => value = Some(d)
    }
  }
  // Assigns a random value based on predefined uper and lower bounds
  def setRandom = {
    if (settable) {
      val v = randomRange(lowerBound, upperBound)
      value = Some(v)
    }
  }
}
