package environment.effect

import environment.effect._
import environment.element._
import environment.layer._

import scoututil.Util._

import scala.math._


class Heat(
  val seed: Temperature
) extends Effect {
  // NOTE: not propagating heat outside of the source
  val range = 0.0

  def calculate(dist: Double): Double = seed.value.getOrElse(0.0)

  def radiate(sourceX: Int, sourceY: Int, layer: Layer, scale: Double) = {
    layer.setElementValue(sourceX, sourceY, seed.value.getOrElse(0.0))
    val cellBlockSize = (range / scale).toInt
    for {
      x <- (sourceX - cellBlockSize) to (sourceX + cellBlockSize)
      y <- (sourceY - cellBlockSize) to (sourceY + cellBlockSize)
      if ((x,y) != (sourceX, sourceY))
      val d: Double = dist(sourceX, sourceY, x, y) * scale
      if (d <= range)
    } layer.setElementValue(x, y, calculate(d))
  }
}
