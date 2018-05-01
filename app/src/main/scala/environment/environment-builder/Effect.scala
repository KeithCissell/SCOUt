package environment.effect

import environment.element._
import environment.layer._


trait Effect {
  val source: Element
  val range: Double
  def calculate(dist: Double): Double
  def radiate(sourceX: Int, sourceY: Int, layer: Layer, scale: Double): Unit
}
