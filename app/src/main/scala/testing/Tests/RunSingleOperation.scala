package test

import io.circe._
import io.circe.parser._
import io.circe.syntax._

import scoutagent._
import scoutagent.State._
import scoutagent.controller._
import environment._
import environment.anomaly._
import environment.element._
import environment.element.seed._
import environment.terrainmodification._
import environment.EnvironmentBuilder._
import test._
import operation._
import scoututil.Util._
import jsonhandler.Encoder._
import jsonhandler.Decoder._
import filemanager.FileManager._
import bestweights._
import scala.collection.mutable.{Map => MutableMap}
import scala.collection.mutable.{ArrayBuffer => AB}
import org.joda.time.DateTime


object RunSingleOperation {

  def getValidStartPosition(env: Environment): (Int,Int) = {
    // Create an agent for validation
    val validationAgent: Agent = new Agent("VALIDATOR", new RandomController())
    // Choose a random point
    val startX = randomInt(0, env.height - 1)
    val startY = randomInt(0, env.width - 1)
    // Check if start position is clear of hazards and doesn't start on an anomaly
    validationAgent.calculateHazardDamage(env, startX, startY, 10000) match {
      case d if (d > 0.0) => return getValidStartPosition(env) // Try different start position
      case d => env.getAnomaliesCluster(startX, startY, 3) match {
        case as if (as.size > 0) => return getValidStartPosition(env)
        case _ => return (startX, startY)
      }
    }
  }

  def main(args: Array[String]): Unit = {

    // Set Agent Variables
    // val controller = new RandomController()
    // val agentName = "Random"
    // val controller = new FindHumanController()
    // val agentName = "Find Human"
    // val controller = new SCOUtController("FHOfficialTemplatesTEST2", "json", false, BestWeights.findHumanLongRun)
    val controller = new SCOUtController("MWOfficialTemplatesTEST2", "json", false, BestWeights.mapWaterLongRun)
    val agentName = "SCOUt"

    val sensors = List(
      new ElevationSensor(false),
      new DecibelSensor(true),
      new TemperatureSensor(true),
      new WaterSensor(false))
    // val sensors = List(
    //   new ElevationSensor(false),
    //   new DecibelSensor(false),
    //   new TemperatureSensor(false),
    //   new WaterSensor(true))

    // DEFAULT
    lazy val defaultEnvironment = buildEnvironment("Test Environment", 20, 20, 10.0, ElementSeedList.defaultSeedList(), TerrainModificationList.defaultList(), AnomalyList.defaultList())
    // LOADED
    val environemtFileName = "MEDIUM"
    // Environment file
    // val envString = readJsonFile(environemtFileName, environmentPath)
    // val environment = parse(envString) match {
    //   case Left(_) => defaultEnvironment // Load or parse failure
    //   case Right(envJson) => extractEnvironment(envJson)
    // }
    // Environment template file
    val templateString = readJsonFile(environemtFileName, environmentTemplatePath)
    val environment = parse(templateString) match {
      case Left(_) => defaultEnvironment // Load or parse failure
      case Right(templateJson) => {
        val template = extractEnvironmentTemplate(templateJson)
        buildEnvironment(template)
      }
    }

    // Set Goal
    // val goalTemplate = new FindAnomaliesTemplate(Map("Human" -> 1), None)
    val goalTemplate = new MapElementsTemplate(List("Water Depth"), None)

    // Setup Operation
    val startPosition = getValidStartPosition(environment)
    val startX = startPosition._1
    val startY = startPosition._2

    val agent = new Agent(
      name = agentName,
      controller = controller,
      sensors = sensors,
      mapHeight = environment.height,
      mapWidth = environment.width,
      mapScale = environment.scale,
      xPosition = startX,
      yPosition = startY)

    val goal = goalTemplate.generateGoal(environment)

    val operation = new Operation(agent, environment, goal, None)

    // Run Operation and save data
    operation.run
    // operation.printActions
    operation.printOutcome
    val fileName = new DateTime().toString("yyyy-MM-dd-HH-mm") + "-" + agentName
    operation.saveOperation(fileName)
  }

}
