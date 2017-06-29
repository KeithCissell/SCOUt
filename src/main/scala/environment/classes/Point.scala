// src\main\scala\environment\point\Point.scala
package environment.point

import scala.math.sqrt
import environment.variable._

case class Point(val x: Int, val y: Int, var variables: Map[String,Option[Variable]]) {

  //        CONSTRUCTOR OVERLOADS
  def this(x: Int, y: Int, vs: Seq[Variable]) {
    this(x, y, (vs.map(_.name) zip vs.map(Some(_))).toMap)
  }

  //        METHODS
  def getX: Int = x
  def getY: Int = y
  def dist(p2: Point): Double = {
    sqrt((p2.getX - x) + (p2.getY - y))
  }
  def get(varName: String): Option[Variable] = variables.contains(varName) match {
    case true => variables(varName)
    case _    => None
  }
  def getAll: Seq[Option[Variable]] = variables.values.toSeq
}
