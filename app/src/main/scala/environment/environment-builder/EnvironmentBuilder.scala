package environment

import environment._
import environment.anomaly._
import environment.cell._
import environment.effect._
import environment.element._
import environment.element.seed._
import environment.layer._
import environment.terrainmodification._

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
      elementSeeds: List[ElementSeed] = Nil,
      terrainModifications: List[TerrainModification] = Nil,
      anomalies: List[Anomaly] = Nil): Environment = {

    // INITIALIZE ENVIRONMENT
    val environment = new Environment(name, height, width, scale)

    // INITIALIZE LAYERS
    println()
    println("Initializing layers...")
    val layers: MutableMap[String,Layer] = MutableMap()
    for (seed <- elementSeeds) {
      val elementType = seed.elementName
      val layer = seed.buildLayer(height, width, scale)
      layers += (elementType -> layer)
    }

    // APPLY TERRAIN MODIFICATIONS
    println()
    println("Applying terrain modifications...")
    val terrainConstructionLayer = new ConstructionLayer(height, width, scale)
    for (mod <- terrainModifications) {
      println()
      println("Starting new mod: " + mod.elementType)
      layers.get(mod.elementType) match {
        case Some(layer) => mod.modify(layer, terrainConstructionLayer)
        case None => println("Layer not found")// Layer not found
      }
    }

    // PLACE ANOMALIES
    println()
    println("Placing anomalies...")
    val anomalyConstructionLayer = new ConstructionLayer(height, width, scale)
    for (anomaly <- anomalies) anomaly.place(environment, layers, anomalyConstructionLayer, scale)

    // CALCULATE MEASUREMENTS


    // SET LAYERS
    for (layer <- layers.values) environment.setLayer(layer)

    // return the generated Environment
    return environment
  }

}
