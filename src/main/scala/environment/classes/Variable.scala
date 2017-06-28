// src\main\scala\environment\variable\Variable.scala
package environment.variable

trait Variable {
  val name: String
  val constant: Boolean
  var value: Double
}


class Height(var value: Double) extends Variable {
  val name = "Height"
  val constant = True
}

class Latitude(var value: Double) extends Variable {
  val name = "Latitude"
  val constant = True
}

class Longitude(var value: Double) extends Variable {
  val name = "Longitude"
  val constant = True
}

class Temperature(var value: Double) extends Variable {
  val name = "Temperature"
  val constant = False
}

class WindSpeed(var value: Double) extends Variable {
  val name = "Wind Speed"
  val constant = False
}











class Template(var value: Double) extends Variable {
  val name = ""
}
