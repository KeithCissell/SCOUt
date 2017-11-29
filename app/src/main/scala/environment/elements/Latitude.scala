package environment.element

import scoututil.Util._
import environment.layer._
import environment.element._
import environment.element.seed._

import scala.collection.mutable.{ArrayBuffer => AB}


class Latitude(var value: Option[Double]) extends Element {
  val name = "Latitude"
  val unit = "°"
  val constant = true
  val circular = true
  val lowerBound = -90.0
  val upperBound = 90.0
  def this(d: Double) = this(Some(d))
  def this()          = this(None)
}

package seed {
  case class LatitudeSeed(
    val elementName: String = "Latitude",
    val dynamic: Boolean = false,
    val rootValue: Double = 1.0,
    val scale: Double = .000003 * 10.0//scale
  ) extends ElementSeed {
    def buildLayer(l: Int, w: Int): Layer = {
      val layer = new Layer(AB.fill(l)(AB.fill(w)(None)))
      for {
        x <- 0 until l
        y <- 0 until w
      } {
        val value = rootValue + (y * scale)
        layer.setElement(x, y, new Latitude(value))
      }
      return layer
    }
  }
}
