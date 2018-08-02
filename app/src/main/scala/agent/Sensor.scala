package agent

import environment._
import environment.element._
import environment.layer._


trait Sensor {
  val elementType: String
  val range: Double // in feet
  val energyExpense: Double
  val runTime: Double // in milliseconds

  def scan(env: Environment, x: Int, y: Int): Layer = {
    val searchLayer = env.getLayer(elementType)
    val searchRadius = if (range / env.scale > 1) range / env.scale else 1.0
    return searchLayer.getClusterLayer(x, y, searchRadius)
  }
}
