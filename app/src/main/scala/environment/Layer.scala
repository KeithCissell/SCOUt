package environment.layer

import scoututil.Util._
import environment.element._

import scala.math._
import scala.collection.mutable.{ArrayBuffer => AB}

class Layer(val layer: Grid[Element]) {
  val height = layer.length
  val width = if (layer.isEmpty) 0 else layer(0).length

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

  def inLayer(x: Int, y: Int): Boolean = {
    x >= 0 && x < height && y >= 0 && y < width
  }

  def getElement(x: Int, y: Int): Option[Element] = {
    if (inLayer(x, y)) {
      layer(x)(y)
    } else None
  }

  def setElement(x: Int, y: Int, e: Element) = {
    if (inLayer(x, y)) {
      layer(x)(y) = Some(e)
    }
  }

  def getCluster(originX: Int, originY: Int, range: Int): List[Element] = {
    (for {
      x <- (originX - range) to (originX + range)
      y <- (originY - range) to (originY + range)
      if dist(x, y, originX, originY) != 0
      if dist(x, y, originX, originY) <= range
    } yield getElement(x, y)).flatten.toList
  }

  def getClusterValues(originX: Int, originY: Int, range: Int): List[Double] = {
    (for {
      x <- (originX - range) to (originX + range)
      y <- (originY - range) to (originY + range)
      if dist(x, y, originX, originY) != 0
      if dist(x, y, originX, originY) <= range
    } yield getElement(x, y).flatMap(_.value)).flatten.toList
  }

}
