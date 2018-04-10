package environment.layer

import scoututil.Util._
import environment.element._

import scala.math._
import scala.collection.mutable.{ArrayBuffer => AB}


class ConstructionElement(
  var modified: Boolean = false
  // var border: Boolean = false,
  // var unmodifiedNeighbor: Boolean = true
)

class ConstructionLayer(
  val height: Int,
  val width:  Int) {

  val layer: AB[AB[ConstructionElement]] = AB.fill(height)(AB.fill(width)(new ConstructionElement()))

  val coordinatePool: AB[(Int,Int)] = (
    for {
      x <- 0 until height
      y <- 0 until width
    } yield (x, y)).to[AB]

  override def toString: String = {
    var str = ""
    for (x <- layer) {
      for (y <- x) {
        str += y.modified.toString + "\t"
      }
      str += "\n"
    }
    return str
  }
  // Checks if (x,y) coordinate is in the layer
  def inLayer(x: Int, y: Int): Boolean = {
    x >= 0 && x < height && y >= 0 && y < width
  }
  // Return the ConstructionElement at (x,y)
  def getElement(x: Int, y: Int): ConstructionElement = {
    layer(x)(y)
  }
  // Checks if element has been modified
  def isModified(x: Int, y: Int): Boolean = {
    if (inLayer(x,y)) layer(x)(y).modified
    else true
  }
  // Returns constructionElements in the given radius from a given origin
  def getCluster(originX: Int, originY: Int, radius: Double): List[ConstructionElement] = {
    (for {
      x <- (originX - Math.round(radius).toInt) to (originX + Math.round(radius).toInt)
      y <- (originY - Math.round(radius).toInt) to (originY + Math.round(radius).toInt)
      if dist(x, y, originX, originY) != 0
      if dist(x, y, originX, originY) <= radius
    } yield layer(x)(y)).toList
  }
  // Like getCluster, but returns the values instead of the entire object
  def getClusterUnmodified(originX: Int, originY: Int, radius: Double): List[ConstructionElement] = {
    (for {
      x <- (originX - Math.round(radius).toInt) to (originX + Math.round(radius).toInt)
      y <- (originY - Math.round(radius).toInt) to (originY + Math.round(radius).toInt)
      if dist(x, y, originX, originY) != 0
      if dist(x, y, originX, originY) <= radius
      if !layer(x)(y).modified
    } yield layer(x)(y)).toList
  }
  // Get a random unmodified neighbor (if any)
  def getUnmodifiedNeighbor(originX: Int, originY: Int): Option[ConstructionElement] = {
    val unmodifiedNeighbors = getClusterUnmodified(originX, originY, 1.5) // 8 neighboring cells
    if (unmodifiedNeighbors.length != 0) {
      val randomIndex = randomInt(0, unmodifiedNeighbors.length - 1)
      Some(unmodifiedNeighbors(randomIndex))
    } else None
  }
  // Finds the next unmodified neighbor in a group of cells (if any)
  def getNextUnmodifiedNeighbor(cells: AB[(Int,Int)]): Option[ConstructionElement] = {
    for (i <- 0 until cells.length) {
      val randomIndex = randomInt(0, cells.length - 1)
      val cell = cells(randomIndex)
      getUnmodifiedNeighbor(cell._1, cell._2) match {
        case Some(bool) => return Some(bool)
        case None => // Check next cell
      }
    }
    return None
  }
}
