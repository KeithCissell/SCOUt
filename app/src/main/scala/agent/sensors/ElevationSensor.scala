package scoutagent

import scoutagent._


class ElevationSensor(
  val indicator: Boolean
) extends Sensor {
  val elementType: String = "Elevation"
  val range: Double = 30.0 // in feet
  val energyExpense: Double = 0.05
  val runTime: Double = 15000 // in milliseconds
  val hazard: Boolean = true
}
