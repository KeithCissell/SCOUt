package agent

import agent._


class TemperatureSensor extends Sensor {
  val elementType: String = "Temperature"
  val range: Double = 30.0 // in feet
  val energyExpense: Double = 0.1
  val runTime: Double = 15000 // in milliseconds
}
