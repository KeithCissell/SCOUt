// src\main\scala\environment\Environment.scala
package environment

import customtypes.Grid._
import environment.point._
import environment.variable._
import scala.collection.mutable.{ArrayBuffer => AB}


class Environment(val name: String, var grid: Grid[Point] = AB(AB(None))) {
  val length = grid.length
  val width = if (grid.isEmpty) 0 else grid(0).length

  //        METHODS
  // returns the grid point at (x,y)
  def getPoint(x: Int, y: Int): Option[Point] = {
    grid(x - 1)(y - 1)
  }
  // returns a set of all variables in the grid
  def getVariableNames: Set[String] = {
    (for {
      p <- grid.flatten.filter(_ != None)
      v <- p.get.variables
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
