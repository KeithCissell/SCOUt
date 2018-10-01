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
  // Test Metrics to gather
  val testMetrics: Map[String,TestMetric] = for ((name,controller) <- controllers) yield name -> new TestMetric(name, AB())

  // Validation Agent used to assure a valid start point is selected
  val validationAgent: Agent = new Agent("VALIDATOR", new RandomController())

  def run: Unit = {
    // Setup environments
    val environments = generateEnvironments
    var runNumber = 0
    for ((environment, iterations) <- environments) {
      for (i <- 0 until iterations) {
        runNumber += 1
        println()
        println(s"Running Test $runNumber")
        val startPosition = getValidStartPosition(environment)
        val startX = startPosition._1
        val startY = startPosition._2
        // Setup agent and run operation
        for ((name, controller) <- controllers) {
          val agent = new Agent(
            name = name,
            controller = controller.copy,
            sensors = sensors,
            mapHeight = environment.height,
            mapWidth = environment.width,
            mapScale = environment.scale,
            xPosition = startX,
            yPosition = startY)
          // OPERATION
          val goal = goalTemplate.generateGoal(environment)
          val operation = new Operation(agent, environment, goal)
          // println(s"Running ${agent.name}")
          operation.run
          testMetrics(name).addRun(operation.runData)
          // operation.printActions
          operation.printOutcome
          // println(s"Stat Position ($startX, $startY)")
          // println(s"End Position (${operation.eventLog.last.state.xPosition}, ${operation.eventLog.last.state.yPosition})")
          // println()
        }
      }
    }
    for ((name,data) <- testMetrics) data.printRunResults
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

  def getValidStartPosition(env: Environment): (Int,Int) = {
    // Choose a random point
    val startX = randomInt(0, env.height - 1)
    val startY = randomInt(0, env.width - 1)
    // Check if start position is clear of hazards and doesn't start on an anomaly
    validationAgent.calculateHazardDamage(env, startX, startY, 10000) match {
      case d if (d > 0.0) => return getValidStartPosition(env) // Try different start position
      case d => env.getAnomalies(startX, startY) match {
        case Some(as) if (as.size > 0) => return getValidStartPosition(env)
        case _ => return (startX, startY)
      }
    }
  }

}

class TestMetric(controllerName: String, runs: AB[RunData]) {
  def addRun(runData: RunData) = runs += runData
  def printRunResults = {
    println()
    println(s"Controller: $controllerName")
    println(s"Runs:       ${runs.size}")
    println(s"Successes:  ${runs.filter(_.successful == true).size}")
    println(s"Avg Success ${runs.map(_.goalCompletion).foldLeft(0.0)(_ + _) / runs.size}")
    println(s"Avg Steps:  ${runs.map(_.steps).foldLeft(0)(_ + _) / runs.size}")
    println(s"Avg Remaining Health: ${runs.map(_.remainingHealth).foldLeft(0.0)(_ + _) / runs.size}")
    println(s"Avg Remaining Energy: ${runs.map(_.remainingEnergy).foldLeft(0.0)(_ + _) / runs.size}")
  }
}

class RunData(val goalCompletion: Double, val steps: Int, val remainingHealth: Double, val remainingEnergy: Double) {
  def successful: Boolean = goalCompletion >= 100.0
}
