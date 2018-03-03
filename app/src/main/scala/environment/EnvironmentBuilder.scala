package environment

import environment._
import environment.cell._
import environment.element._
import environment.element.seed._

import scoututil.Util._

import scala.util.Random
import scala.collection.mutable.{ArrayBuffer => AB}


object EnvironmentBuilder {

  def buildEnvironment(name: String, height: Int, width: Int, scale: Double = 1.0, seeds: List[ElementSeed] = Nil): Environment = {
    val environment = new Environment(name, height, width, scale)
    for (seed <- seeds) {
      val layer = seed.buildLayer(height, width, scale)
      environment.setLayer(layer)
    }
    return environment
  }

}
