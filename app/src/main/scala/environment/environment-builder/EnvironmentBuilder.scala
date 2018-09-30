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
    val layers: MutableMap[String,Layer] = MutableMap()
    for (seed <- elementSeeds) {
      val elementType = seed.elementName
      val layer = seed.buildLayer(height, width, scale)
      layers += (elementType -> layer)
    }

    // APPLY TERRAIN MODIFICATIONS
    val terrainConstructionLayer = new ConstructionLayer(height, width, scale)
    for (mod <- terrainModifications) {
      layers.get(mod.elementType) match {
        case Some(layer) => mod.modify(layer, terrainConstructionLayer)
        case None => // Layer not found
      }
    }

    // PLACE ANOMALIES
    val anomalyConstructionLayer = new ConstructionLayer(height, width, scale)
    for (anomaly <- anomalies) anomaly.place(environment, layers, anomalyConstructionLayer, scale)

    // CALCULATE MEASUREMENTS


    // SET LAYERS
    for (layer <- layers.values) environment.setLayer(layer)

    // return the generated Environment
    return environment
  }

  def buildEnvironment(template: EnvironmentTemplate): Environment = {
    buildEnvironment(template.name, template.height, template.width, template.scale, template.elementSeeds, template.terrainModifications, template.anomalies)
  }

}
