package environment.effect

import environment.element._
import environment.layer._


trait Effect {
  val seed: Element
  val range: Double

  // calculate the value of a cell x distance away from the seed
  def calculate(dist: Double): Double

  // adjust values on a layer based on the seed value and the range
  def radiate(sourceX: Int, sourceY: Int, layer: Layer, scale: Double): Unit
}
