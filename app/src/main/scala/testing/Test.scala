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
  val goalTemplate: GoalTemplate,
  val maxActions: Option[Int],
  val verbose: Boolean = false
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
        if (verbose) {
          println()
          println()
          println(s"Running Test $runNumber")
        }
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
          val operation = new Operation(agent, environment, goal, maxActions)
          // println(s"Running ${agent.name}")
          operation.run
          testMetrics(name).addRun(operation.runData)
          // operation.printActions
          if (verbose) operation.printOutcome
          // println(s"Start Position ($startX, $startY)")
          // println(s"End Position (${operation.eventLog.last.state.xPosition}, ${operation.eventLog.last.state.yPosition})")
          // println()
        }
      }
    }
    if (verbose) for ((name,data) <- testMetrics) data.printRunResults
  }

  // Generate Environments: environment -> iterations to run
  def generateEnvironments: Map[Environment,Int] = {
    if (verbose) println("GENERATING ENVIRONMENTS...")
    var environments: MutableMap[Environment,Int] = MutableMap()
    // Load environment files
    if (verbose) println("LOADING FROM FILES...")
    for ((fName, iterations) <- testEnvironments) {
      if (verbose) println(s"Loading from: $fName")
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
    if (verbose) println("LOADING FROM TEMPLATES...")
    for ((fName, iterations) <- testTemplates) {
      if (verbose) println(s"Loading from: $fName")
      val templateString = readJsonFile(fName, environmentTemplatePath)
      val buildIterations = iterations._1
      val testIterations = iterations._2
      parse(templateString) match {
        case Left(_) => // Load or parse failure
        case Right(templateJson) => {
          val template = extractEnvironmentTemplate(templateJson)
          for (i <- 0 until buildIterations) {
            if (verbose) println(s"    loading ${i+1} / $buildIterations")
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
      case d => env.getAnomaliesCluster(startX, startY, 3) match {
        case as if (as.size > 0) => return getValidStartPosition(env)
        case _ => return (startX, startY)
      }
    }
  }

}

class TestMetric(controllerName: String, runs: AB[RunData]) {
  def addRun(runData: RunData) = runs += runData
  def avgGoalCompletion: Double = runs.map(_.goalCompletion).foldLeft(0.0)(_ + _) / runs.size
  def avgActions: Int = runs.map(_.steps).foldLeft(0)(_ + _) / runs.size
  def avgRemainingHealth: Double = runs.map(_.remainingHealth).foldLeft(0.0)(_ + _) / runs.size
  def avgRemainingEnergy: Double = runs.map(_.remainingEnergy).foldLeft(0.0)(_ + _) / runs.size
  def printRunResults = {
    println()
    println(s"Controller: $controllerName")
    println(s"Runs:       ${runs.size}")
    println(s"Successes:  ${runs.filter(_.successful == true).size}")
    println(s"Avg Steps:  ${avgActions}")
    println(s"Avg Remaining Health: ${roundDouble2(avgRemainingHealth)}")
    println(s"Avg Remaining Energy: ${roundDouble2(avgRemainingEnergy)}")
    println(s"AVG SUCCESS RATE:     ${roundDouble2(avgGoalCompletion)}")
  }
}

class RunData(val goalCompletion: Double, val steps: Int, val remainingHealth: Double, val remainingEnergy: Double) {
  def successful: Boolean = goalCompletion >= 100.0
}
