package environment.element

import scoututil.Util._

import scala.math.BigDecimal


// Element Trait
trait Element {
  var value: Option[Double]
  val name: String
  val unit: String
  val constant: Boolean
  val radial: Boolean
  val lowerBound: Double
  val upperBound: Double
  // val color: String = "#1BBA09" // Hex value for color in gui

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
    if (radial) d match {
      case v if v < lowerBound  => set(v + (upperBound - lowerBound))
      case v if v > upperBound  => set(v - (upperBound - lowerBound))
      case _                    => value = Some(roundDouble2(d))
    } else d match {
      case v if v < lowerBound  => value = Some(lowerBound)
      case v if v > upperBound  => value = Some(upperBound)
      case _                    => value = Some(roundDouble2(d))
    }
  }
  // Assigns a random value based on predefined uper and lower bounds
  def setRandom = {
    if (settable) {
      val v = randomDouble(lowerBound, upperBound)
      value = Some(roundDouble2(v))
    }
  }
}

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
