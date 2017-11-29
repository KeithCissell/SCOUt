package environment.element

import scoututil.Util._
import environment.layer._
import environment.element._
import environment.element.seed._

import scala.collection.mutable.{ArrayBuffer => AB}


class Temperature(var value: Option[Double]) extends Element {
  val name = "Temperature"
  val unit = "°F"
  val constant = false
  val circular = false
  val lowerBound = -200.0
  val upperBound = 200.0
  def this(d: Double) = this(Some(d))
  def this()          = this(None)
}

package seed {
  case class TemperatureSeed(
    val elementName: String = "Temperature",
    val dynamic: Boolean = true,
    val average: Double = 70.0,
    val deviation: Double = 0.2
  ) extends ElementSeed {
    def randomDeviation(mean: Double): Double = {
      val lowerBound = mean - deviation
      val upperBound = mean + deviation
      randomRange(lowerBound, upperBound)
    }
    def buildLayer(l: Int, w: Int): Layer = {
      val layer = new Layer(AB.fill(l)(AB.fill(w)(None)))
      if (l > 0 && w > 0) layer.setElement(0, 0, new Temperature(average))
      for {
        x <- 0 until l
        y <- 0 until w
        if (x,y) != (0,0)
      } {
        val cluster = layer.getClusterValues(x, y, 3)
        val mean = cluster.sum / cluster.length
        val value = randomDeviation(mean)
        layer.setElement(x, y, new Temperature(value))
      }
      // Smooth the layer
      for {
        x <- 0 until l
        y <- 0 until w
      } {
        val cluster = layer.getClusterValues(x, y, 3)
        val mean = cluster.sum / cluster.length
        layer.setElement(x, y, new Temperature(mean))
      }
      return layer
    }
  }
}
