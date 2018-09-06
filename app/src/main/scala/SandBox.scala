package sandbox

import io.circe._
import io.circe.syntax._

import agent._
import agent.controller._
import environment._
import environment.anomaly._
import environment.element._
import environment.element.seed._
import environment.terrainmodification._
import environment.EnvironmentBuilder._
import operation._
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
      controller = new RandomController(),
      sensors = List(
        new ElevationSensor(),
        new DecibelSensor(),
        new TemperatureSensor(),
        new WaterSensor()),
      mapHeight = height,
      mapWidth = width,
      xPosition = randomInt(0, height - 1),
      yPosition = randomInt(0, width - 1))

    // GOAL
    val goal = new FindAnomalies(Map("Human" -> 1), None)
    // val goal = new MapElements(height, width, List("Elevation"), None)

    // OPERATION
    val operation = new Operation(testBot, testEnv, goal)
    // operation.run

    val scoutController = new SCOUtController("stateActionTest")
    scoutController.loadMemory()
    scoutController.saveMemory()

    // Messing with filemanager
    // val encodedEnv = encodeEnvironment(testEnv)
    // saveJsonFile("test", "src/resources/environments/", encodedEnv)
    // val envStringFromFile = readJsonFile("test", "src/resources/environments/")
    // val envFromFile = parse(envStringFromFile) match {
    //   case Left(_) => None
    //   case Right(e) => Some(extractEnvironment(e))
    // }
    // println(envFromFile)

    // Save State Actions
    // val stateActionJson = Json.fromValues(operation.getStateActionPairs().map(_.toJson()))
    // saveJsonFile("stateActionTest", "src/resources/agent-memory/", stateActionJson)

  }

}
