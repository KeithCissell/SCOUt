package agent

import environment._
import environment.element._
import environment.layer._
import scoututil.Util._


trait Sensor {
  val elementType: String
  val range: Double // in feet
  val energyExpense: Double
  val runTime: Double // in milliseconds
  val indicator: Boolean
  val hazard: Boolean

  def scan(env: Environment, x: Int, y: Int): Layer = {
    val searchLayer = env.getLayer(elementType)
    val searchRadius = Math.max(range / env.scale, 1.0)
    return searchLayer.getClusterLayer(x, y, searchRadius)
  }

  def cellRange(env: Environment, originX: Int, originY: Int): Int = {
    var cellCount = 0
    val searchRadius = Math.max(range / env.scale, 1.0)
    val cellBlockSize = Math.round(Math.abs(searchRadius)).toInt
    for {
      x <- (originX - cellBlockSize) to (originX + cellBlockSize)
      y <- (originY - cellBlockSize) to (originY + cellBlockSize)
      if dist(x, y, originX, originY) <= searchRadius
    } if (env.inGrid(x, y)) cellCount += 1
    return cellCount
  }
}
