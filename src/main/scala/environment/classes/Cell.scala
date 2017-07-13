// src\main\scala\environment\Cell\Cell.scala
package environment.cell

import environment.variable._

import scala.math._
import scala.collection.mutable.{ArrayBuffer => AB}


case class Cell(val x: Int, val y: Int,
    var variables: AB[Variable] = AB.empty) {

  override def toString: String = {
    var variablesString = ""
    for (v <- variables) variablesString += s"\n\t${v.name}: ${v.value} ${v.unit}"
    return s"\nCell ($x, $y)\nVariables:$variablesString"
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
  def get(s: String): Option[Variable] = {
    var result: Option[Variable] = None
    for (v <- variables) if (v.name == s) result = Some(v)
    return result
  }
}
