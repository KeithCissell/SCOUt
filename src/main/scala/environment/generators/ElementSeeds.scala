package environment.generator

import environment.element._


object ElementSeeds {

  trait ElementSeed

  case class LatitudeSeed() extends ElementSeed {
    val elementName = "Latitude"
  }

  case class LongitudeSeed() {}

}
