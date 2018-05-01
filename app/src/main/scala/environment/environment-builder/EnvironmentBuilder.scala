package environment

import environment._
import environment.cell._
import environment.element._
import environment.element.seed._
import environment.layer._
import environment.anomalies._
import environment.modifications._

import scoututil.Util._

import scala.util.Random
import scala.collection.mutable.{ArrayBuffer => AB}
import scala.collection.mutable.{Map => MutableMap}


object EnvironmentBuilder {

  def buildEnvironment(
      name: String,
      height: Int,
      width: Int,
      scale: Double = 1.0,
      seeds: List[ElementSeed] = Nil): Environment = {

    // INITIALIZE ENVIRONMENT
    val environment = new Environment(name, height, width, scale)

    // INITIALIZE LAYERS
    val layers: MutableMap[String,Layer] = MutableMap()
    
    for (seed <- seeds) {
      val elementType = seed.elementName
      val layer = seed.buildLayer(height, width, scale)
      layers += elementType -> layer
    }

    // APPLY TERRAIN MODIFICATIONS
    //------------------------------------------------------------------------
    val terrainModifications: List[TerrainModification] = List(
      ElevationModification(modification = 150.0, deviation = 20.0, coverage = 0.3, slope = 26.0),
      ElevationModification(modification = -30.0, deviation = 3.0, coverage = 0.17, slope = 10.0),
      WaterPoolModification(maxDepth = 10.0, deviation = 2.5, coverage = 0.15, slope = 3.0),
      WaterStreamModification(depth = 5.0, deviation = 2.0, width = 30.0, length = 1000.0)
    )
    //------------------------------------------------------------------------
    val terrainConstructionLayer = new ConstructionLayer(height, width, scale)

    for (mod <- terrainModifications) layers.get(mod.elementType) match {
      case None => // Layer not found
      case Some(layer) => mod.modify(layer, terrainConstructionLayer)
    }

    // PLACE ANOMALIES


    // CALCULATE MEASUREMENTS


    // SET LAYERS
    for (layer <- layers.values) environment.setLayer(layer)

    // return the generated Environment
    return environment
  }

}
