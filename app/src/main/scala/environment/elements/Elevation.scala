package environment.element

import scoututil.Util._
import environment.layer._
import environment.element._
import environment.element.seed._

import scala.collection.mutable.{ArrayBuffer => AB}


class Elevation(var value: Option[Double]) extends Element {
  val name = "Elevation"
  val unit = "ft"
  val constant = true
  val circular = false
  val lowerBound = -1500.0
  val upperBound = 1500.0
  def this(d: Double) = this(Some(d))
  def this()          = this(None)
}

package seed {
  case class ElevationSeed(
    val elementName: String = "Elevation",
    val dynamic: Boolean = false,
    val average: Double = 0.0,
    val deviation: Double = 0.15 * 10.0//scale
  ) extends ElementSeed {

    def randomDeviation(mean: Double): Double = {
      val lowerBound = mean - deviation
      val upperBound = mean + deviation
      randomRange(lowerBound, upperBound)
    }
    def buildLayer(l: Int, w: Int): Layer = {
      val layer = new Layer(AB.fill(l)(AB.fill(w)(None)))
      if (l > 0 && w > 0) layer.setElement(0, 0, new Elevation(average))
      for {
        x <- 0 until l
        y <- 0 until w
        if (x,y) != (0,0)
      } {
        val cluster = layer.getClusterValues(x, y, 3)
        val mean = cluster.sum / cluster.length
        val value = randomDeviation(mean)
        layer.setElement(x, y, new Elevation(value))
      }
      // Smooth the layer
      for {
        x <- 0 until l
        y <- 0 until w
      } {
        val cluster = layer.getClusterValues(x, y, 3)
        val mean = cluster.sum / cluster.length
        layer.setElement(x, y, new Elevation(mean))
      }
      return layer
    }

  }
}
