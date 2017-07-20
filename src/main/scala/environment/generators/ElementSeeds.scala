package environment.generator

import environment.element._

/*
Defaults within this program are generated with a scale of 10 ft. between each point.
*/


object ElementSeeds {

  trait ElementSeed {
    val elementName: String
    val dynamic: Boolean
  }

  //case class NoinseSource(val x: Int, val y: Int, val value: Double)

  case class DecibleSeed(
    val elementName: String = "Decible",
    val dynamic: Boolean = true
    //val sources: List[NoiseSource] = Nil
  ) extends ElementSeed {}

  case class ElevationSeed(
    val elementName: String = "Elevation",
    val dynamic: Boolean = false,
    val average: Double = 0.0,
    val deviation: Double = 1.5
  ) extends ElementSeed {}

  case class LatitudeSeed(
    val elementName: String = "Latitude",
    val dynamic: Boolean = false,
    val rootValue: Double = 1.0,
    val scale: Double = .00003
  ) extends ElementSeed {}

  case class LongitudeSeed(
    val elementName: String = "Longitude",
    val dynamic: Boolean = false,
    val rootValue: Double = 1.0,
    val scale: Double = .00003
  ) extends ElementSeed {}

}
