package environment.element

import myutil.Util._

import scala.math.BigDecimal


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


class Decible(var value: Option[Double]) extends Element {
  val name = "Decible"
  val unit = "dB"
  val constant = false
  val circular = false
  val lowerBound = 0.0
  val upperBound = 120.0
  def this(d: Double) = this(Some(d))
  def this()          = this(None)
  override def set(d: Double) = {
    if (settable) d match {
      case v if v < lowerBound  => value = Some(lowerBound)
      case v if v > upperBound  => value = Some(upperBound)
      case _                    => value = Some(d)
    }
  }
}

class Elevation(var value: Option[Double]) extends Element {
  val name = "Elevation"
  val unit = "ft"
  val constant = true
  val circular = false
  val lowerBound = -1500.0
  val upperBound = 1500.0
  def this(d: Double) = this(Some(d))
  def this()          = this(None)
}

class Latitude(var value: Option[Double]) extends Element {
  val name = "Latitude"
  val unit = "°"
  val constant = true
  val circular = true
  val lowerBound = -90.0
  val upperBound = 90.0
  def this(d: Double) = this(Some(d))
  def this()          = this(None)
}

class Longitude(var value: Option[Double]) extends Element {
  val name = "Longitude"
  val unit = "°"
  val constant = true
  val circular = true
  val lowerBound = -180.0
  val upperBound = 180.0
  def this(d: Double) = this(Some(d))
  def this()          = this(None)
}

class Temperature(var value: Option[Double]) extends Element {
  val name = "Temperature"
  val unit = "°F"
  val constant = false
  val circular = false
  val lowerBound = -200.0
  val upperBound = 200.0
  def this(d: Double) = this(Some(d))
  def this()          = this(None)
}

class WindDirection(var value: Option[Double]) extends Element {
  val name = "Wind Direction"
  val unit = "° from N"
  val constant = false
  val circular = true
  val lowerBound = 0.0
  val upperBound = 360.0
  def this(d: Double) = this(Some(d))
  def this()          = this(None)
}

class WindSpeed(var value: Option[Double]) extends Element {
  val name = "Wind Speed"
  val unit = "MPH"
  val constant = false
  val circular = false
  val lowerBound = 0.0
  val upperBound = 200.0
  def this(d: Double) = this(Some(d))
  def this()          = this(None)
}
