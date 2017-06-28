// src\main\scala\environment\variable\Variable.scala
package environment.variable

trait Variable {
  val name: String
  var value: Double
}


class Height(var value: Double) extends Variable {
  val name = "Height"
}

class Latitude(var value: Double) extends Variable {
  val name = "Latitude"
}

class Longitude(var value: Double) extends Variable {
  val name = "Longitude"
}

class Temperature(var value: Double) extends Variable {
  val name = "Temperature"
}

class WindSpeed(var value: Double) extends Variable {
  val name = "Wind Speed"
}











class Template(var value: Double) extends Variable {
  val name = ""
}
