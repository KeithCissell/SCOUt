package agent

import agent._


class ElevationSensor extends Sensor {
  val elementType: String = "Elevation"
  val range: Double = 30.0 // in feet
  val energyExpense: Double = 0.05
  val runTime: Double = 15000 // in milliseconds
}
