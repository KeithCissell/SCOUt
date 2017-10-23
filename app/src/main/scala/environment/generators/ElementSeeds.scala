package environment.generator

import myutil.Util._
import environment.element._

import scala.math._
import scala.util.Random
import scala.collection.mutable.{ArrayBuffer => AB}


object ElementSeeds {

  trait ElementSeed {
    val elementName: String
    val dynamic: Boolean
  }


  // NOTE:  Defaults within this program are generated with a scale of 10 ft. between each Cell.
  //        The scale can be adjusted but all equations are relative to ft.
  implicit val scale = 10.0


  case class NoiseSource(val x: Int, val y: Int, val value: Double)

  case class DecibleSeed(
    val elementName: String = "Decible",
    val dynamic: Boolean = true,
    val scale: Double = scale,
    val lowerBound: Double = 0.0,
    val upperBound: Double = 120.0,
    val randomSources: Int = 5,
    val sources: AB[NoiseSource] = AB.empty
  ) extends ElementSeed {
    // NOTE: using noise ruduction of -6 dB every doubling of distance
    // http://www.sengpielaudio.com/calculator-distance.htm
    def soundReduction(source: NoiseSource, x: Int, y: Int): Double = {
      val cellDist = dist(source.x, source.y, x, y) * scale
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

  case class ElevationSeed(
    val elementName: String = "Elevation",
    val dynamic: Boolean = false,
    val average: Double = 0.0,
    val deviation: Double = 0.15 * scale
  ) extends ElementSeed {
    def randomDeviation(mean: Double): Double = {
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
  }

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

  // List of all seed defaults
  val defaultSeedList: List[ElementSeed] = List(
    DecibleSeed(),
    ElevationSeed(),
    LatitudeSeed(),
    LongitudeSeed(),
    TemperatureSeed(),
    WindDirectionSeed(),
    WindSpeedSeed()
  )

}
