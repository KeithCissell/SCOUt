// src\main\scala\environment\Environment.scala
package environment

import environment.point._
import environment.variable._
import scala.collection.mutable.{ArrayBuffer => AB}

object GridType {
  type Grid[A] = AB[AB[Option[A]]]
}

import environment.GridType.Grid

class Environment(val name: String, val length: Int,
    val width: Int, var grid: Grid[Point]) {

  //        CONSTRUCTOR OVERLOADS
  def this(s: String, l: Int, w: Int) {
    this(s, l, w, AB.fill(l)(AB.fill(w)(None)))
  }

  //        METHODS
  // returns the grid point at (x,y)
  def getPoint(x: Int, y: Int): Option[Point] = {
    grid(x)(y)
  }
  // returns a set of all variables in the grid
  def getVariableNames: Set[String] = {
    (for {
      p   <- grid.flatten.filter(_ != None)
      vs  <- p.get.getAll.filter(_ != None)
      v   <- vs
    } yield v.name).toSet
  }
  // returns a grid with a specified variable at each point
  def getLayer(variable: String): Grid[Variable] = {
    var layer: Grid[Variable] = AB.fill(length)(AB.fill(width)(None))
    for {
      x <- 0 until length
      y <- 0 until width
    } layer(x)(y) = grid(x)(y) match {
      case Some(p)  => p.get(variable)
      case None     => None
      case _        => None
    }
    return layer
  }

}
