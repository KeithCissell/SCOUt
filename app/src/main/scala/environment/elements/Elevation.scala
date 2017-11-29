package environment.element

import scoututil.Util._
import environment.element._
import environment.element.seed._


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
  }
}
