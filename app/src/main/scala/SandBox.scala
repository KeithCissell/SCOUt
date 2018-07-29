package sandbox

import io.circe._
import io.circe.parser._
import io.circe.syntax._

import agent._
import environment._
import environment.anomaly._
import environment.element._
import environment.element.seed._
import environment.terrainmodification._
import environment.EnvironmentBuilder._
import agent.Robot._
import environment._
import scoututil.Util._
import jsonhandler.Encoder._
import jsonhandler.Decoder._
import filemanager.FileManager._
import scala.collection.mutable.{ArrayBuffer => AB}


object SandBox {

  def main(args: Array[String]): Unit = {

    // Environment Variables
    val height = 20
    val width = 20
    val scale = 10.0
    var terrainModificationList: List[TerrainModification] = TerrainModificationList.defaultList()
    var elementSeedList: List[ElementSeed] = ElementSeedList.defaultSeedList()
    var anomalyList: List[Anomaly] = AnomalyList.defaultList()

    // ENVIRONMENT
    val testEnv = buildEnvironment("Test Environment", height, width, scale, elementSeedList, terrainModificationList, anomalyList)

    // AGENT
    val testBot = new Robot(
      sensors = List(
        new ElevationSensor(),
        new DecibelSensor(),
        new TemperatureSensor(),
        new WaterSensor()),
      mapHeight = height,
      mapWidth = width,
      movementCost = 0.01,
      slopeDamageThreshHold = -3.0,
      durability = 1.0,
      xPosition = randomInt(0, height),
      yPosition = randomInt(0, width),
      energyLevel = 100.0,
      health = 100.0,
      eventLog = AB())

    // Log Robot State
    // println(testBot.getState())

    // Give robot commands
    testBot.move(testEnv, randomInt(0, height), randomInt(0, width))
    testBot.scan(testEnv, "Elevation")
    testBot.move(testEnv, randomInt(0, height), randomInt(0, width))
    testBot.scan(testEnv, "Water")
    testBot.move(testEnv, randomInt(0, height), randomInt(0, width))
    testBot.scan(testEnv, "Decibel")

    //Log Robot State
    // println(testBot.getState())



    // Playing with Json stuff
    val encodedEnv = encodeEnvironment(testEnv)
    // val decodedEnv = parse(encodedEnv) match {
    //   case Left(_) => None
    //   case Right(e) => Some(extractEnvironment(e))
    // }



    // Messing with filemanager
    saveJsonFile("test", "SavedEnvironments/Environment/", encodedEnv)
    val envStringFromFile = readJsonFile("test", "SavedEnvironments/Environment/")
    val envFromFile = parse(envStringFromFile) match {
      case Left(_) => None
      case Right(e) => Some(extractEnvironment(e))
    }
    println(envFromFile)

  }

}
