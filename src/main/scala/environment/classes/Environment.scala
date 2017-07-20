package environment

import customtypes.Grid._
import environment.cell._
import environment.layer._
import environment.element._

import scala.collection.mutable.{ArrayBuffer => AB}


class Environment(val name: String, private var grid: Grid[Cell]) {

  val length = grid.length
  val width = if (grid.isEmpty) 0 else grid(0).length

  override def toString: String = {
    var str = name + "\n"
    for {
      x <- 0 until length
      y <- 0 until width
    } getCell(x, y) match {
      case Some(c)  => str += c.toString + "\n"
      case None     => str += s"No Cell Found at: ($x, $y)\n"
    }
    return str
  }

  //        METHODS
  // return copy of the grid
  def getGrid: Grid[Cell] = grid
  // returns the grid Cell at (x,y)
  def getCell(x: Int, y: Int): Option[Cell] = { //(x-1,y-1) match {
    if (x >= 0 && x < length && y >= 0 && y < width) {
      grid(x)(y)
    } else None
  }
  // returns a set of all elements in the grid
  def getElementNames: Set[String] = {
    (for {
      p <- grid.flatten.filter(_ != None)
      v <- p.get.getElementNames
    } yield v).toSet
  }
  // returns a grid with a specified element at each Cell
  def getLayer(element: String): Layer = {
    var layer = new Layer(AB.fill(length)(AB.fill(width)(None)))
    for {
      x <- 0 until length
      y <- 0 until width
    } layer.layer(x)(y) = grid(x)(y) match {
      case Some(p)  => p.get(element)
      case None     => None
      case _        => None
    }
    return layer
  }
  // returns all Cells in range of an origin
  def getCluster(originX: Int, originY: Int, range: Int): Seq[Cell] = {
    val refCell = new Cell(originX, originY)
    val cluster = for {
      x <- -range to range
      y <- -range to range
      if refCell.dist(originX + x, originY + y) <= range
      if refCell.dist(originX + x, originY + y) != 0
    } yield getCell(originX + x, originY + y)
    return cluster.flatten
  }
  // add a variable to a given point
  def setElement(x: Int, y: Int, element: Element) = getCell(x, y) match {
    case Some(e)  => grid(x)(y).get.setElement(element)
    case None     => grid(x)(y) = Some(new Cell(x, y, Map(element.name -> element)))
  }
  // takes a layer of elements and adds it to the environment
  def setLayer(layer: Layer) = {
    for {
      x <- 0 until length
      y <- 0 until width
    } layer.getElement(x, y) match {
      case Some(e)  => setElement(x, y, e)
      case None     => // Nothing to do here
    }
  }

}
