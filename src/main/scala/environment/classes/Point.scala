// src\main\scala\environment\point\Point.scala
package environment.point

import scala.math._
import environment.variable._
import scala.collection.mutable.{ArrayBuffer => AB}


case class Point(val x: Int, val y: Int,
    var variables: AB[Variable] = AB.empty) {

  def getX: Int = x
  def getY: Int = y
  def dist(p2: Point): Double = {
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
