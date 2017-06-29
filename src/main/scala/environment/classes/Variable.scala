// src\main\scala\environment\variable\Variable.scala
package environment.variable

trait Variable {
  val name: String
  val constant: Boolean
  var value: Option[Double]

  def set(d: Double) = {
    if (!constant || value == None) { value = Some(d) }
  }

}


class Height(var value: Option[Double]) extends Variable {
  val name = "Height"
  val constant = true
  def this(d: Double) = this(Some(d))
  def this()          = this(None)
}

class Latitude(var value: Option[Double]) extends Variable {
  val name = "Latitude"
  val constant = true
  def this(d: Double) = this(Some(d))
  def this()          = this(None)
}

class Longitude(var value: Option[Double]) extends Variable {
  val name = "Longitude"
  val constant = true
  def this(d: Double) = this(Some(d))
  def this()          = this(None)
}

class Temperature(var value: Option[Double]) extends Variable {
  val name = "Temperature"
  val constant = false
  def this(d: Double) = this(Some(d))
  def this()          = this(None)
}

class WindSpeed(var value: Option[Double]) extends Variable {
  val name = "Wind Speed"
  val constant = false
  def this(d: Double) = this(Some(d))
  def this()          = this(None)
}











class Template(var value: Option[Double]) extends Variable {
  val name = ""
  val constant = false
  def this(d: Double) = this(Some(d))
  def this()          = this(None)
}
