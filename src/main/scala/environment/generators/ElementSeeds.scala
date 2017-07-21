package environment.generator

import myutil.RandomRange._
import environment.element._

import scala.math._
import scala.util.Random


object ElementSeeds {

  trait ElementSeed {
    val elementName: String
    val dynamic: Boolean
  }

  // Defaults within this program are generated with a scale of 10 ft. between each Cell.
  implicit val scale = 10.0


  case class NoiseSource(val x: Int, val y: Int, val value: Double)

  case class DecibleSeed(
    val elementName: String = "Decible",
    val dynamic: Boolean = true,
    val randomSources: Int = 1,
    val sources: List[NoiseSource] = Nil
  ) extends ElementSeed {
    private def dist(x1: Int, y1: Int, x2: Int, y2: Int): Double = {
      sqrt(pow(x2 - x1, 2.0) + pow(y2 - y1, 2.0))
    }
    def log2(x: Double): Double = log(x) / log(2)
    // NOTE: using noise ruduction of -6 dB every doubling of distance
    // http://www.sengpielaudio.com/calculator-distance.htm
    def soundReduction(source: NoiseSource, x: Int, y: Int): Double = {
      val distance = dist(source.x, source.y, x, y)
      source.value - (abs(log2(distance)) * 6)
    }
    def createRandomSource(length: Int, width: Int) = {
      val randX = round(randomRange(0, length-1))
      val randY = round(randomRange(0, width-1))
      
    }
  }

  case class ElevationSeed(
    val elementName: String = "Elevation",
    val dynamic: Boolean = false,
    val average: Double = 0.0,
    val deviation: Double = .15 * scale
  ) extends ElementSeed {
    def randomDeviation(deviation: Double, mean: Double): Double = {
      val lowerBound = mean - deviation
      val upperBound = mean + deviation
      randomRange(lowerBound, upperBound)
    }
  }

  case class LatitudeSeed(
    val elementName: String = "Latitude",
    val dynamic: Boolean = false,
    val rootValue: Double = 1.0,
    val scale: Double = .000003 * scale
  ) extends ElementSeed {}

  case class LongitudeSeed(
    val elementName: String = "Longitude",
    val dynamic: Boolean = false,
    val rootValue: Double = 1.0,
    val scale: Double = .000003 * scale
  ) extends ElementSeed {}

}
