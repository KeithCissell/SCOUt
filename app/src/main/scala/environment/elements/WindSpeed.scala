package environment.element

import scoututil.Util._
import environment.element._
import environment.element.seed._


class WindSpeed(var value: Option[Double]) extends Element {
  val name = "Wind Speed"
  val unit = "MPH"
  val constant = false
  val circular = false
  val lowerBound = 0.0
  val upperBound = 200.0
  def this(d: Double) = this(Some(d))
  def this()          = this(None)
}

package seed {
  case class WindSpeedSeed(
    val elementName: String = "Wind Speed",
    val dynamic: Boolean = true,
    val average: Double = 3.0,
    val deviation: Double = 0.2
  ) extends ElementSeed {
    def randomDeviation(mean: Double): Double = {
      val lowerBound = mean - deviation
      val upperBound = mean + deviation
      randomRange(lowerBound, upperBound)
    }
  }
}
