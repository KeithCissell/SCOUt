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
    "Water Depth" -> true,
    "Wind Direction" -> false,
    "Wind Speed" -> false
  )

  def normalizeElementValue(elementType: String, value: Option[Double]): Option[Double] = value match {
    case None => None
    case Some(v) => getSampleElement(elementType) match {
      case None => None
      case Some(element) => Some(v / (element.upperBound - element.lowerBound))
    }
  }

  def getSampleElement(elementType: String): Option[Element] = elementType match {
    case "Elevation" => Some(new Elevation())
    case "Latitude" => Some(new Latitude())
    case "Longitude" => Some(new Longitude())
    case "Decibel" => Some(new Decibel())
    case "Temperature" => Some(new Temperature())
    case "Water Depth" => Some(new WaterDepth())
    case "Wind Direction" => Some(new WindDirection())
    case "Wind Speed" => Some(new WindSpeed())
    case _ => None
  }
}
