package agent

import agent._


class DecibelSensor extends Sensor {
  val elementType: String = "Decibel"
  val range: Double = 15.0 // in feet
  val energyExpense: Double = 0.01
  val runTime: Double = 100 // in milliseconds
}
