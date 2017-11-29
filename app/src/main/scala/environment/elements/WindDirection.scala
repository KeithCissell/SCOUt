package environment.element

import scoututil.Util._
import environment.element._
import environment.element.seed._


class WindDirection(var value: Option[Double]) extends Element {
  val name = "Wind Direction"
  val unit = "° from N"
  val constant = false
  val circular = true
  val lowerBound = 0.0
  val upperBound = 360.0
  def this(d: Double) = this(Some(d))
  def this()          = this(None)
}

package seed {
  case class WindDirectionSeed(
    val elementName: String = "Wind Direction",
    val dynamic: Boolean = true,
    val average: Double = 0.0,
    val deviation: Double = 1.5
  ) extends ElementSeed {
    def randomDeviation(mean: Double): Double = {
      val lowerBound = mean - deviation
      val upperBound = mean + deviation
      randomRange(lowerBound, upperBound)
    }
  }
}
