package environment.effect

import environment.effect._
import environment.element._
import environment.layer._

import scoututil.Util._

import scala.math._


class Sound(
  val source: Decibel
) extends Effect {
  // NOTE: using noise ruduction of -6 dB every doubling of distance
  // http://www.sengpielaudio.com/calculator-distance.htm
  val range = pow(2, source.value.getOrElse(0.0) / 6)
  def calculate(dist: Double): Double = roundDouble2(source.value.getOrElse(0.0) - (abs(log2(dist)) * 6))

  def radiate(sourceX: Int, sourceY: Int, layer: Layer, scale: Double) = {
    val cellBlockSize = (range / scale).toInt
    for {
      x <- (sourceX - cellBlockSize) to (sourceX + cellBlockSize)
      y <- (sourceY - cellBlockSize) to (sourceY + cellBlockSize)
      if ((x,y) != (0,0))
      val d: Double = dist(sourceX, sourceY, x, y) * scale
      if (d <= range)
    } layer.setElementValue(x, y, calculate(d))
  }
}
