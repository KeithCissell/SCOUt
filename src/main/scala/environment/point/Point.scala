// src\main\scala\environment\point\Point.scala
package environment.point

import scala.math.sqrt
import environment.variable._

case class Point(val x: Int, val y: Int) {
  def getX: Int = x
  def getY: Int = y
  def dist(p2: Point): Double = sqrt((p2.getX - x) + (p2.getY - y))
}

class EnvironmentPoint(x: Int, y: Int, var evs: Map[String,Variable])
    extends Point(x,y) {
  def this(x: Int, y: Int, vs: Seq[Variable]) {
    this(x, y, (vs.map(_.name) zip vs).toMap)
  }
}
