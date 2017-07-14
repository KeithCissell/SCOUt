package environment.cell

import environment.element._

import scala.math._
import scala.collection.mutable.{ArrayBuffer => AB}


case class Cell(val x: Int, val y: Int,
    var elements: AB[Element] = AB.empty) {

  override def toString: String = {
    var elementsString = ""
    for (v <- elements) elementsString += s"\n\t${v.name}: ${v.value} ${v.unit}"
    return s"\nCell ($x, $y)\nElements:$elementsString"
  }

  def getX: Int = x
  def getY: Int = y
  def dist(p2: Cell): Double = {
    val x1 = x.toDouble
    val y1 = y.toDouble
    val x2 = p2.getX.toDouble
    val y2 = p2.getY.toDouble
    sqrt(pow(x2 - x1, 2.0) + pow(y2 - y1, 2.0))
  }
  def dist(xInt: Int, yInt: Int): Double = {
    val x1 = x.toDouble
    val y1 = y.toDouble
    val x2 = xInt.toDouble
    val y2 = yInt.toDouble
    sqrt(pow(x2 - x1, 2.0) + pow(y2 - y1, 2.0))
  }
  def get(s: String): Option[Element] = {
    var result: Option[Element] = None
    for (v <- elements) if (v.name == s) result = Some(v)
    return result
  }
}
