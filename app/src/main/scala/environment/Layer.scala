package environment.layer

import scoututil.Util._
import environment.element._

import scala.math._
import scala.collection.mutable.{ArrayBuffer => AB}


class Layer(val layer: Grid[Element]) {
  val height: Int = layer.length
  val width:  Int = if (layer.isEmpty) 0 else layer(0).length

  val coordinatePool: AB[(Int,Int)] = (
    for {
      x <- 0 until height
      y <- 0 until width
    } yield (x, y)).to[AB]

  override def toString: String = {
    var str = ""
    for (x <- layer) {
      for (y <- x) {
        str += y.getOrElse("NONE") + "\t"
      }
      str += "\n"
    }
    return str
  }
  // Checks if (x,y) coordinate is in the layer
  def inLayer(x: Int, y: Int): Boolean = {
    x >= 0 && x < height && y >= 0 && y < width
  }
  // Returns the element object at the (x,y) coordinate
  def getElement(x: Int, y: Int): Option[Element] = {
    if (inLayer(x, y)) {
      layer(x)(y)
    } else None
  }
  // Returns the value of the element at (x,y)
  def getElementValue(x: Int, y: Int): Option[Double] = {
    if (inLayer(x, y)) {
      layer(x)(y) match {
        case Some(element) => element.value
        case None => None
      }
    } else None
  }
  // Sets (x,y) to the given element
  def setElement(x: Int, y: Int, e: Element) = {
    if (inLayer(x, y)) {
      layer(x)(y) = Some(e)
    }
  }
  // Sets element value at (x,y)
  def setElementValue(x: Int, y: Int, v: Double) = {
    if (inLayer(x, y)) {
      layer(x)(y) match {
        case Some(element) => layer(x)(y).get.set(v)
        case None => None
      }
    }
  }
  // Returns any elements in the given radius from a given origin
  def getCluster(originX: Int, originY: Int, radius: Double): List[Element] = {
    val cellBlockSize = Math.round(Math.abs(radius)).toInt
    (for {
      x <- (originX - cellBlockSize) to (originX + cellBlockSize)
      y <- (originY - cellBlockSize) to (originY + cellBlockSize)
      if dist(x, y, originX, originY) != 0
      if dist(x, y, originX, originY) <= radius
    } yield getElement(x, y)).flatten.toList
  }
  // Like getCluster, but returns the values instead of the entire object
  def getClusterValues(originX: Int, originY: Int, radius: Double): List[Double] = {
    val cellBlockSize = Math.round(Math.abs(radius)).toInt
    (for {
      x <- (originX - cellBlockSize) to (originX + cellBlockSize)
      y <- (originY - cellBlockSize) to (originY + cellBlockSize)
      if dist(x, y, originX, originY) != 0
      if dist(x, y, originX, originY) <= radius
    } yield getElement(x, y).flatMap(_.value)).flatten.toList
  }
  // Like getCluster, but returns a layer including only those in the search radius
  def getClusterLayer(originX: Int, originY: Int, radius: Double): Layer = {
    val clusterLayer = new Layer(AB.fill(height)(AB.fill(width)(None)))
    val cellBlockSize = Math.round(Math.abs(radius)).toInt
    for {
      x <- (originX - cellBlockSize) to (originX + cellBlockSize)
      y <- (originY - cellBlockSize) to (originY + cellBlockSize)
      if dist(x, y, originX, originY) <= radius
    } getElement(x, y) match {
      case None => // No element found
      case Some(e) => clusterLayer.setElement(x, y, e)
    }
    return clusterLayer
  }
  // Adjust value at (x,y) by a weighted average of neighboring cells
  def smooth(x: Int, y: Int, radius: Double, originWeight: Double) = getElementValue(x,y) match {
    case None => // Nothing to do
    case Some(currentValue) => {
      val cluster = getClusterValues(x, y, radius)
      val newValue = (currentValue * originWeight + cluster.sum) / (originWeight + cluster.length)
      setElementValue(x, y, newValue)
    }
  }
  // Smooth cells directly adjacent to (x,y)
  def smoothNeighbors(originX: Int, originY: Int, radius: Double, originWeight: Double) = {
    smooth(originX + 1, originY, radius, originWeight)
    smooth(originX - 1, originY, radius, originWeight)
    smooth(originX, originY + 1, radius, originWeight)
    smooth(originX, originY - 1, radius, originWeight)
  }
  // Smooth a list of given cells
  def smoothArea(cells: AB[(Int,Int)], radius: Double, originWeight: Double) = {
    var pool = cells.clone()
    for (i <- 0 until cells.length) {
      // Randomly select a cell from list smooth
      val randomIndex = randomInt(0, pool.length - 1)
      val cellCoordinates = pool.remove(randomIndex)
      val x = cellCoordinates._1
      val y = cellCoordinates._2
      smooth(x, y, radius, originWeight)
    }
  }
  // Smooth a list of given cells and their neighboring cells
  def smoothAreaNeighbors(cells: AB[(Int,Int)], radius: Double, originWeight: Double) = {
    var pool = cells.clone()
    for (i <- 0 until cells.length) {
      // Randomly select a cell from list smooth
      val randomIndex = randomInt(0, pool.length - 1)
      val cellCoordinates = pool.remove(randomIndex)
      val x = cellCoordinates._1
      val y = cellCoordinates._2
      smooth(x, y, radius, originWeight)
      smoothNeighbors(x, y, radius, originWeight)
    }
  }
  // Smooth all cells in layer
  def smoothLayer(radius: Double, originWeight: Double) = {
    smoothArea(coordinatePool, radius, originWeight)
  }
}
