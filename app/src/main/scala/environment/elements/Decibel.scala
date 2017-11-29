package environment.element

import scoututil.Util._
import environment.element._
import environment.element.seed._


class Decibel(var value: Option[Double]) extends Element {
  val name = "Decibel"
  val unit = "dB"
  val constant = false
  val circular = false
  val lowerBound = 0.0
  val upperBound = 120.0
  def this(d: Double) = this(Some(d))
  def this()          = this(None)
}

package seed {
  case class NoiseSource(val x: Int, val y: Int, val value: Double)

  case class DecibleSeed(
    val elementName: String = "Decible",
    val dynamic: Boolean = true,
    val scale: Double = 10.0,//scale,
    val lowerBound: Double = 0.0,
    val upperBound: Double = 120.0,
    val randomSources: Int = 5,
    val sources: AB[NoiseSource] = AB.empty
  ) extends ElementSeed {
    // NOTE: using noise ruduction of -6 dB every doubling of distance
    // http://www.sengpielaudio.com/calculator-distance.htm
    def soundReduction(source: NoiseSource, x: Int, y: Int): Double = {
      val cellDist = dist(source.x, source.y, x, y) * 10.0//scale
      val range = soundRange(source)
      if (cellDist <= range) {
        return roundDouble2(source.value - (abs(log2(cellDist)) * 6))
      } else return 0.0
    }
    def soundRange(source: NoiseSource): Double = {
      pow(2, source.value / 6)
    }
    def createRandomSource(length: Int, width: Int) = {
      val randX = round(randomRange(0, length-1)).toInt
      val randY = round(randomRange(0, width-1)).toInt
      val randValue = randomRange(upperBound, lowerBound)
      sources += NoiseSource(randX, randY, randValue)
    }
  }
}
