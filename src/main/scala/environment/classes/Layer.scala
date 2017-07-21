package environment.layer

import customtypes.Grid._
import environment.element._

import scala.math._
import scala.collection.mutable.{ArrayBuffer => AB}

class Layer(val layer: Grid[Element]) {
  val length = layer.length
  val width = if (layer.isEmpty) 0 else layer(0).length

  override def toString: String = {
    var str = ""
    for (x <- layer) {
      for (y <- x) {
        str += y.get + "\t"
      }
      str += "\n"
    }
    return str
  }

  private def inLayer(x: Int, y: Int): Boolean = {
    x >= 0 && x < length && y >= 0 && y < width
  }
  
  private def dist(x1: Int, y1: Int, x2: Int, y2: Int): Double = {
    sqrt(pow(x2 - x1, 2.0) + pow(y2 - y1, 2.0))
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

  def getCluster(originX: Int, originY: Int, range: Int): List[Double] = {
    (for {
      x <- -range to range
      y <- -range to range
      if dist(originX + x, originY + y, originX, originY) <= range
    } yield getElement(x, y).flatMap(_.value)).flatten.toList
  }

}
