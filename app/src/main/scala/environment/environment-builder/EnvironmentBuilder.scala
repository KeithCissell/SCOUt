package environment

import environment._
import environment.cell._
import environment.element._
import environment.element.seed._
import environment.layer._
import environment.modifications._

import scoututil.Util._

import scala.util.Random
import scala.collection.mutable.{ArrayBuffer => AB}


object EnvironmentBuilder {

  def buildEnvironment(
      name: String,
      height: Int,
      width: Int,
      scale: Double = 1.0,
      seeds: List[ElementSeed] = Nil): Environment = {

    // Initialize environment
    val environment = new Environment(name, height, width, scale)

    // Generate Terrain
    //------------------------------------------------------------------------
    val terrainModifications: List[TerrainModification] = List(
      ElevationModification(modification = 150.0, deviation = 20.0, coverage = 0.3, slope = 26.0),
      ElevationModification(modification = -30.0, deviation = 3.0, coverage = 0.17, slope = 10.0),
      WaterPoolModification(maxDepth = 10.0, deviation = 2.5, coverage = 0.15, slope = 3.0),
      WaterStreamModification(depth = 5.0, deviation = 2.0, width = 30.0, length = 1000.0)
    )
    //------------------------------------------------------------------------
    val terrainLayers: List[Layer] = generateTerrain(height, width, scale, terrainModifications)
    for (layer <- terrainLayers) environment.setLayer(layer)

    // Place Anomolies

    // Calculate measurements
    for (seed <- seeds) {
      val layer = seed.buildLayer(height, width, scale)
      environment.setLayer(layer)
    }

    // return the generated Environment
    return environment
  }


  def generateTerrain(height: Int, width: Int, scale: Double, modifications: List[TerrainModification]): List[Layer] = {
    // params TEMP
    //------------------------------------------------------------------------
    val rootValue = 0.0
    val baseDeviation = 3.0
    //------------------------------------------------------------------------

    // CREATE CONSTRUCTION LAYER
    val constructionLayer = new ConstructionLayer(height, width, scale)

    // INITIALIZE ELEVATION LAYER
    val elevation: Layer = new Layer(AB.fill(height)(AB.fill(width)(None)))
    for {
      x <- 0 until height
      y <- 0 until width
    } {
      val randomElevation = randomDouble(rootValue - baseDeviation, rootValue + baseDeviation)
      elevation.setElement(x, y, new Elevation(randomElevation))
    }
    elevation.smoothLayer(3, 3)

    // INITIALIZE WATERDEPTH LAYER
    val waterDepth: Layer = new Layer(AB.fill(height)(AB.fill(width)(Some(new WaterDepth(0.0)))))

    // APPLY MODIFICATIONS
    for (mod <- modifications) mod match {
      case mod: ElevationModification => mod.modify(elevation, constructionLayer)
      case mod: WaterPoolModification =>  mod.modify(waterDepth, constructionLayer)
      case mod: WaterStreamModification =>  mod.modify(waterDepth, constructionLayer)
    }

    // Return the constructed layers
    return List(elevation, waterDepth)
  }

}
