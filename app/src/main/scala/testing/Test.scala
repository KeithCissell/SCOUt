package test

import io.circe._
import io.circe.parser._
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
import scala.collection.mutable.{Map => MutableMap}
import scala.collection.mutable.{ArrayBuffer => AB}


class Test(
  val testEnvironments: Map[String,Int],    // environmentFileName -> test iterations
  val testTemplates: Map[String,(Int,Int)], // templateFileName -> (build iterations, test iteration per build)
  val controllers: Map[String,Controller],  // agentName -> controller
  val sensors: List[Sensor],
  val goalTemplate: GoalTemplate
) {
  def run: Unit = {
    // Setup environments
    val environments = generateEnvironments
    for ((environment, iterations) <- environments) {
      for (i <- 0 until iterations) {
        val startX = randomInt(0, environment.height - 1)
        val startY = randomInt(0, environment.width - 1)
        // Setup robot and run operation
        for ((name, controller) <- controllers) {
          val robot = new Robot(
            name = name,
            controller = controller,
            sensors = sensors,
            mapHeight = environment.height,
            mapWidth = environment.width,
            mapScale = environment.scale,
            xPosition = startX,
            yPosition = startY)
          // OPERATION
          val goal = goalTemplate.generateGoal(environment)
          val operation = new Operation(robot, environment, goal)
          operation.run
          // operation.printOutcome
        }
      }
    }
  }

  // Generate Environments: environment -> iterations to run
  def generateEnvironments: Map[Environment,Int] = {
    var environments: MutableMap[Environment,Int] = MutableMap()
    // Load environment files
    for ((fName, iterations) <- testEnvironments) {
      val envString = readJsonFile(fName, environmentPath)
      parse(envString) match {
        case Left(_) => // Load or parse failure
        case Right(envJson) => {
          val env = extractEnvironment(envJson)
          environments  += (env -> iterations)
        }
      }
    }
    // Load template files
    for ((fName, iterations) <- testTemplates) {
      val templateString = readJsonFile(fName, environmentTemplatePath)
      val buildIterations = iterations._1
      val testIterations = iterations._2
      parse(templateString) match {
        case Left(_) => // Load or parse failure
        case Right(templateJson) => {
          val template = extractEnvironmentTemplate(templateJson)
          for (i <- 0 until buildIterations) {
            val env = buildEnvironment(template)
            environments += (env -> testIterations)
          }
        }
      }
    }
    return environments.toMap
  }

}
