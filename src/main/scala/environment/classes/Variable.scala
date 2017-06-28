// src\main\scala\environment\variable\Variable.scala
package environment.variable

trait Variable {
  val name: String
}


class Height(var value: Double) extends Variable {
  val name = "Height"
}

class Location(var latitude: Double, var longitude: Double) extends Variable {
  val name = "Location"
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
