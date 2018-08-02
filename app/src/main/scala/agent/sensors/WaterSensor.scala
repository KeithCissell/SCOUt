package agent

import agent._


class WaterSensor extends Sensor {
  val elementType: String = "Water"
  val range: Double = 1.0 // in feet
  val energyExpense: Double = 0.5
  val runTime: Double = 30000 // in milliseconds
}
