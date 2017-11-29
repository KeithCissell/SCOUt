package environment.element.seed

import scoututil.Util._
import environment.element._

import scala.math._
import scala.util.Random
import scala.collection.mutable.{ArrayBuffer => AB}


// List of all seed defaults
object DefaultSeedList {
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

trait ElementSeed {
  val elementName: String
  val dynamic: Boolean
}
