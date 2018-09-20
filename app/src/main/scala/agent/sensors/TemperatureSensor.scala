package scoutagent

import scoutagent._


class TemperatureSensor(
  val indicator: Boolean
) extends Sensor {
  val elementType: String = "Temperature"
  val range: Double = 30.0 // in feet
  val energyExpense: Double = 0.1
  val runTime: Double = 15000 // in milliseconds
  val hazard: Boolean = true
}
