package scoutagent

import scoutagent._


class WaterSensor(
  val indicator: Boolean
) extends Sensor {
  val elementType: String = "Water Depth"
  val range: Double = 1.0 // in feet
  val energyExpense: Double = 0.5
  val runTime: Double = 30000 // in milliseconds
  val hazard: Boolean = true
}
