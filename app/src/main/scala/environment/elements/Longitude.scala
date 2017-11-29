package environment.element

import scoututil.Util._
import environment.element._
import environment.element.seed._


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
    val scale: Double = .000003 * 10.0//scale
  ) extends ElementSeed {}
}
