// src\main\scala\environment\Environment.scala
package environment

import customtypes.Grid._
import environment.cell._
import environment.variable._

import scala.collection.mutable.{ArrayBuffer => AB}


class Environment(val name: String, var grid: Grid[Cell] = AB(AB(None))) {
  val length = grid.length
  val width = if (grid.isEmpty) 0 else grid(0).length

  //        METHODS
  // returns the grid Cell at (x,y)
  def getCell(x: Int, y: Int): Option[Cell] = { //(x-1,y-1) match {
    if (x >= 1 && x <= length && y >= 1 && y <= width) {
      grid(x-1)(y-1)
    } else None
  }
  // returns a set of all variables in the grid
  def getVariableNames: Set[String] = {
    (for {
      p <- grid.flatten.filter(_ != None)
      v <- p.get.variables
    } yield v.name).toSet
  }
  // returns a grid with a specified variable at each Cell
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
  // returns all Cells in range of an origin
  def getCluster(p: Cell, range: Int): Seq[Cell] = {
    val origin = (p.getX, p.getY)
    val cluster = for {
      x <- -range to range
      y <- -range to range
      if p.dist(origin._1 + x, origin._2 + y) <= range
      if p.dist(origin._1 + x, origin._2 + y) != 0
    } yield getCell(origin._1 + x, origin._2 + y)
    return cluster.flatten
  }
  // returns all Cells in range of an origin (takes Ints instead of a Cell)
  def getCluster(originX: Int, originY: Int, range: Int): Seq[Cell] = {
    val refCell = new Cell(originX, originY)
    val origin = (originX, originY)
    val cluster = for {
      x <- -range to range
      y <- -range to range
      if refCell.dist(origin._1 + x, origin._2 + y) <= range
      if refCell.dist(origin._1 + x, origin._2 + y) != 0
    } yield getCell(origin._1 + x, origin._2 + y)
    return cluster.flatten
  }

}
