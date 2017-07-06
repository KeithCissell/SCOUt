// src\main\scala\environment\point\Point.scala
package environment.point

import scala.math.sqrt
import environment.variable._
import scala.collection.mutable.{ArrayBuffer => AB}


case class Point(val x: Int, val y: Int,
    var variables: AB[Variable] = AB.empty) {

  def getX: Int = x
  def getY: Int = y
  def dist(p2: Point): Double = {
    sqrt((p2.getX - x) + (p2.getY - y))
  }
  def get(s: String): Option[Variable] = {
    var result: Option[Variable] = None
    for (v <- variables) if (v.name == s) result = Some(v)
    return result
  }
}
