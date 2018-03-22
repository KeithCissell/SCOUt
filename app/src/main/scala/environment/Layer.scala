package environment.layer

import scoututil.Util._
import environment.element._

import scala.math._
import scala.collection.mutable.{ArrayBuffer => AB}

class Layer(val layer: Grid[Element]) {
  val height: Int = layer.length
  val width: Int = if (layer.isEmpty) 0 else layer(0).length

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
  def getCluster(originX: Int, originY: Int, radius: Int): List[Element] = {
    (for {
      x <- (originX - radius) to (originX + radius)
      y <- (originY - radius) to (originY + radius)
      if dist(x, y, originX, originY) != 0
      if dist(x, y, originX, originY) <= radius
    } yield getElement(x, y)).flatten.toList
  }
  // Like getCluster, but returns the values instead of the entire object
  def getClusterValues(originX: Int, originY: Int, radius: Int): List[Double] = {
    (for {
      x <- (originX - radius) to (originX + radius)
      y <- (originY - radius) to (originY + radius)
      if dist(x, y, originX, originY) != 0
      if dist(x, y, originX, originY) <= radius
    } yield getElement(x, y).flatMap(_.value)).flatten.toList
  }
  // Adjust value at (x,y) by a weighted average of neighboring cells
  def smooth(x: Int, y: Int, radius: Int, originWeight: Double) = getElementValue(x,y) match {
    case Some(currentValue) => {
      val cluster = getClusterValues(x, y, radius)
      val newValue = (currentValue * originWeight + cluster.sum) / (originWeight + cluster.length)
      setElementValue(x, y, newValue)
    }
    case None => // Nothing to do
  }

}
