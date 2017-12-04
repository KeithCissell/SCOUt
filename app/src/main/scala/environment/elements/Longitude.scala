package environment.element

import scoututil.Util._
import environment.layer._
import environment.element._
import environment.element.seed._

import scala.collection.mutable.{ArrayBuffer => AB}


class Longitude(var value: Option[Double]) extends Element {
  val name = "Longitude"
  val unit = "°"
  val constant = true
  val circular = true
  val lowerBound = -180.0
  val upperBound = 180.0
  def this(d: Double) = this(Some(d))
  def this()          = this(None)
}

package seed {
  case class LongitudeSeed(
    val elementName: String = "Longitude",
    val dynamic: Boolean = false,
    val rootValue: Double = 1.0,
    // val scale: Double = .000003 * 10.0//scale
  ) extends ElementSeed {
    def buildLayer(height: Int, width: Int, scale: Double): Layer = {
      val layer = new Layer(AB.fill(height)(AB.fill(width)(None)))
      for {
        x <- 0 until height
        y <- 0 until width
      } {
        val value = rootValue + (x * .000003 * scale)
        layer.setElement(x, y, new Longitude(value))
      }
      return layer
    }
  }
}
