// src\main\scala\environment\variable\Variable.scala
package environment.variable

import scala.util.Random
import scala.math.BigDecimal


trait Variable {
  var value: Option[Double]
  val name: String
  val unit: String
  val constant: Boolean
  val lowerBound: Double
  val upperBound: Double

  def set(d: Double) = {
    if (!constant || value == None) { value = Some(d) }
  }

  def random = {
    if (!constant || value == None) {
      val v = lowerBound + (upperBound - lowerBound) * Random.nextDouble
      val roundV = BigDecimal(v).setScale(2, BigDecimal.RoundingMode.HALF_UP).toDouble
      value = Some(roundV)
    }
  }

}


class Height(var value: Option[Double]) extends Variable {
  val name = "Height"
  val unit = "mi"
  val constant = true
  val lowerBound = -15.0
  val upperBound = 15.0
  def this(d: Double) = this(Some(d))
  def this()          = this(None)
}

class Latitude(var value: Option[Double]) extends Variable {
  val name = "Latitude"
  val unit = "°"
  val constant = true
  val lowerBound = -90.0
  val upperBound = 90.0
  def this(d: Double) = this(Some(d))
  def this()          = this(None)
}

class Longitude(var value: Option[Double]) extends Variable {
  val name = "Longitude"
  val unit = "°"
  val constant = true
  val lowerBound = -180.0
  val upperBound = 180.0
  def this(d: Double) = this(Some(d))
  def this()          = this(None)
}

class Temperature(var value: Option[Double]) extends Variable {
  val name = "Temperature"
  val unit = "°F"
  val constant = false
  val lowerBound = -200.0
  val upperBound = 200.0
  def this(d: Double) = this(Some(d))
  def this()          = this(None)
}

class WindSpeed(var value: Option[Double]) extends Variable {
  val name = "Wind Speed"
  val unit = "MPH"
  val constant = false
  val lowerBound = 0.0
  val upperBound = 1000.0
  def this(d: Double) = this(Some(d))
  def this()          = this(None)
}











class Template(var value: Option[Double]) extends Variable {
  val name = ""
  val unit = ""
  val constant = false
  val lowerBound = 0.0
  val upperBound = 0.0
  def this(d: Double) = this(Some(d))
  def this()          = this(None)
}
