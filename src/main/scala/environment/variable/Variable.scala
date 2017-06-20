// src\main\scala\environment\variable\Variable.scala
package environment.variable

trait Variable {
  val name: String
  var value: Double
}

class WindSpeed(var value: Double) extends Variable {
  val name = "Wind Speed"
}
