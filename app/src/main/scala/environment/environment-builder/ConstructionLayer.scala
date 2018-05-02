package environment.layer

import scoututil.Util._
import environment.element._

import scala.math._
import scala.collection.mutable.{ArrayBuffer => AB}
import scala.collection.mutable.{Set => MutableSet}


class ConstructionCell(
  var modified: Boolean = false,
  var elements: MutableSet[String] = MutableSet()
  // var border: Boolean = false,
  // var unmodifiedNeighbor: Boolean = true
)

class ConstructionLayer(
  val height: Int,
  val width:  Int,
  val scale: Double) {

  val layer: AB[AB[ConstructionCell]] = AB.fill(height)(AB.fill(width)(new ConstructionCell()))
  val cellCount = height * width
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
  // Checks if cell has been modified
  def isModified(x: Int, y: Int): Boolean = {
    if (inLayer(x, y)) layer(x)(y).modified
    else true
  }
  // Checks if cell contains a certain type
  def isElement(x: Int, y: Int, element: String): Boolean = {
    if (inLayer(x, y)) layer(x)(y).elements.contains(element)
    else false
  }
  // Checks cell is a border cell (has unmodified neighbors)
  def isBorder(x: Int, y: Int): Boolean = {
    // checks 8 adjacent neighbors
    if (inLayer(x, y)) getClusterUnmodified(x, y, 1.5).length match {
      case 0 => false
      case _ => true
    } else false
  }
  // Checks cell is a border cell of a specific element type (has unmodified neighbors)
  def isBorder(x: Int, y: Int, element: String): Boolean = {
    // checks 8 adjacent neighbors
    if (inLayer(x, y)) getClusterUnmodified(x, y, 1.5, element).length match {
      case 0 => false
      case _ => true
    } else false
  }
  // Sets a cells status to modified = true
  def setToModified(x: Int, y: Int) = {
    if (inLayer(x, y)) layer(x)(y).modified = true
  }
  // Sets a cells status to modified = true
  def setToModified(x: Int, y: Int, element: String) = {
    if (inLayer(x, y)) {
      layer(x)(y).modified = true
      layer(x)(y).elements ++ element
    }
  }
  // Returns a random unmodified cell (if any)
  def getRandomUnmodified(): Option[(Int,Int)] = {
    var cells = coordinatePool.clone()
    for (i <- 0 until cells.length) {
      val randomIndex = randomInt(0, cells.length - 1)
      val c = cells.remove(randomIndex)
      if (!isModified(c._1, c._2)) return Some(c)
    }
    return None
  }
  // Returns constructionCells in the given radius from a given origin
  def getCluster(originX: Int, originY: Int, radius: Double): List[ConstructionCell] = {
    val cellBlockSize = Math.round(Math.abs(radius)).toInt
    (for {
      x <- (originX - cellBlockSize) to (originX + cellBlockSize)
      y <- (originY - cellBlockSize) to (originY + cellBlockSize)
      if dist(x, y, originX, originY) != 0
      if dist(x, y, originX, originY) <= radius
    } yield layer(x)(y)).toList
  }
  // Returns constructionCells in the given radius from a given origin of a specific element type
  def getCluster(originX: Int, originY: Int, radius: Double, element: String): List[ConstructionCell] = {
    val cellBlockSize = Math.round(Math.abs(radius)).toInt
    (for {
      x <- (originX - cellBlockSize) to (originX + cellBlockSize)
      y <- (originY - cellBlockSize) to (originY + cellBlockSize)
      if dist(x, y, originX, originY) != 0
      if dist(x, y, originX, originY) <= radius
      if layer(x)(y).elements.contains(element)
    } yield layer(x)(y)).toList
  }
  // Like getCluster, but returns the values instead of the entire object
  def getClusterUnmodified(originX: Int, originY: Int, radius: Double): List[(Int,Int)] = {
    val cellBlockSize = Math.round(Math.abs(radius)).toInt
    (for {
      x <- (originX - cellBlockSize) to (originX + cellBlockSize)
      y <- (originY - cellBlockSize) to (originY + cellBlockSize)
      if inLayer(x, y)
      if dist(x, y, originX, originY) != 0
      if dist(x, y, originX, originY) <= radius
      if !layer(x)(y).modified
    } yield (x, y)).toList
  }
  // Like getCluster, but returns the values instead of the entire object
  def getClusterUnmodified(originX: Int, originY: Int, radius: Double, element: String): List[(Int,Int)] = {
    val cellBlockSize = Math.round(Math.abs(radius)).toInt
    (for {
      x <- (originX - cellBlockSize) to (originX + cellBlockSize)
      y <- (originY - cellBlockSize) to (originY + cellBlockSize)
      if inLayer(x, y)
      if dist(x, y, originX, originY) != 0
      if dist(x, y, originX, originY) <= radius
      if !layer(x)(y).modified
      if layer(x)(y).elements.contains(element)
    } yield (x, y)).toList
  }
  // Get a random unmodified neighbor (if any)
  def getUnmodifiedNeighbor(originX: Int, originY: Int): Option[(Int,Int)] = {
    val unmodifiedNeighbors = getClusterUnmodified(originX, originY, 1.5) // 8 neighboring cells
    if (unmodifiedNeighbors.length != 0) {
      val randomIndex = randomInt(0, unmodifiedNeighbors.length - 1)
      Some(unmodifiedNeighbors(randomIndex))
    } else None
  }
  // Finds the next unmodified neighbor in a group of cells (if any)
  def getNextUnmodifiedNeighbor(cells: AB[(Int,Int)]): Option[(Int,Int)] = {
    var cellsClone = cells.clone()
    for (i <- 0 until cells.length) {
      val randomIndex = randomInt(0, cellsClone.length - 1)
      val cell = cellsClone.remove(randomIndex)
      getUnmodifiedNeighbor(cell._1, cell._2) match {
        case Some(c) => return Some(c)
        case None => // Check next cell
      }
    }
    return None
  }
  // Finds an unmodified neighbor and returns the direction towards it (if any)
  def getDirectionToUnmodified(x: Int, y: Int): Option[Double] = getUnmodifiedNeighbor(x, y) match {
    case Some(c) => Some(directionDegrees(x, y, c._1, c._2))
    case None => None
  }
  // Gets an unmodified neighbor with a directional influence (if any)
  def getUnmodifiedNeighborDirectional(originX: Int, originY: Int, direction: Double, directionalConstraint: Double): Option[(Int,Int)] = {
    val lowerBound = normalizeDegrees(direction - (directionalConstraint / 2))
    val upperBound = normalizeDegrees(direction + (directionalConstraint / 2))
    val unmodifiedNeighbors = getClusterUnmodified(originX, originY, 1.5) // 8 neighboring cells
    var cells: AB[(Int,Int)] = (unmodifiedNeighbors).to[AB]
    for (i <- 0 until unmodifiedNeighbors.length) {
      val randomIndex = randomInt(0, cells.length - 1)
      val c = cells.remove(randomIndex)
      val dir = directionDegrees(originX, originY, c._1, c._2)
      if (inRangeDegrees(dir, lowerBound, upperBound)) return Some(c)
    }
    return None
  }
  // Finds the next unmodified neighbor of a given element type (if any)
  def getRandomBorder(element: String): Option[(Int,Int)] = {
    var cells = coordinatePool.clone()
    for (i <- 0 until coordinatePool.length) {
      val randomIndex = randomInt(0, cells.length - 1)
      val cell = cells.remove(randomIndex)
      // if (cell._1 == 0 | cell._1 == height) return Some(cell)
      // if (cell._2 == 0 | cell._2 == width) return Some(cell)
      if (isElement(cell._1, cell._2, element) && isBorder(cell._1, cell._2)) return Some(cell)
    }
    return None
  }
}
