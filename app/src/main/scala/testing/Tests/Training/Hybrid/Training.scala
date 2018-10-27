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
import weighttuning.WeightTuning._
import scala.collection.mutable.{Map => MutableMap}
import scala.collection.mutable.{ArrayBuffer => AB}


object HybridTraining {

  def main(args: Array[String]): Unit = {

    // Training Setup
    val trainingIterations = 30
    val controllerName = "SCOUt"
    val memoryFileName = "Hybrid"
    val weightsSet = BestWeights.hybridLongRun

    val agentSensors1 = List(
      new ElevationSensor(false),
      new DecibelSensor(true),
      new TemperatureSensor(true),
      new WaterSensor(false))

    val agentSensors2 = List(
      new ElevationSensor(false),
      new WaterSensor(true))

    val testEnvironments: Map[String,Int] = Map()
    val testTemplates: Map[String,(Int,Int)] = Map(
      "EASY" -> (20, 1),
      "MEDIUM" -> (20, 1),
      "HARD" -> (20, 1)
    )

    val trainingEnvironments: Map[String,Int] = Map()
    val trainingTemplates = Map(
      "EASY" -> (1, 1), // note: half the number of tests as non-hybrid (bc. it's run twice)
      "MEDIUM" -> (1, 1),
      "HARD" -> (1, 1)
    )

    val goalTemplate1 = new FindAnomaliesTemplate(Map("Human" -> 1), None)
    val goalTemplate2 = new MapElementsTemplate(List("Water Depth"), None)

    // Training Performance Data
    var avgSuccess: AB[Double] = AB()
    var avgActions: AB[Int] = AB()
    var avgRemainingHealth: AB[Double] = AB()
    var avgRemainingEnergy: AB[Double] = AB()

    var hAvgSuccess: AB[Double] = AB()
    var hAvgActions: AB[Int] = AB()
    var hAvgRemainingHealth: AB[Double] = AB()
    var hAvgRemainingEnergy: AB[Double] = AB()

    var rAvgSuccess: AB[Double] = AB()
    var rAvgActions: AB[Int] = AB()
    var rAvgRemainingHealth: AB[Double] = AB()
    var rAvgRemainingEnergy: AB[Double] = AB()

    // TRAINING LOOP
    for (i <- 0 until trainingIterations) {

      // Run a Training Test
      println()
      println()
      println(s"********** TRAINING ${i+1} ***********")
      val trainingSensors = if (i % 2 == 0) agentSensors1 else agentSensors2
      val trainingGoal = if (i % 2 == 0) goalTemplate1 else goalTemplate2
      val training = new Test(
        testEnvironments = trainingEnvironments,
        testTemplates = trainingTemplates,
        controllers = Map(
          controllerName -> new SCOUtController(memoryFileName, "json", true, weightsSet)),
        sensors = trainingSensors,
        goalTemplate = trainingGoal,
        maxActions = None
      )

      training.run

      // Test Controller
      println()
      println()
      println(s"********** TESTING ${i+1} ***********")
      val iterationTest1 = new Test(
        testEnvironments = testEnvironments,
        testTemplates = testTemplates,
        controllers = Map(
          "Random" -> new RandomController(),
          "Heuristic" -> new FindHumanController(),
          controllerName -> new SCOUtController(memoryFileName, "json", false, weightsSet)),
        sensors = agentSensors1,
        goalTemplate = goalTemplate1,
        maxActions = None,
        verbose = true
      )
      val iterationTest2 = new Test(
        testEnvironments = testEnvironments,
        testTemplates = testTemplates,
        controllers = Map(
          "Random" -> new RandomController(),
          "Heuristic" -> new FindHumanController(),
          controllerName -> new SCOUtController(memoryFileName, "json", false, weightsSet)),
        sensors = agentSensors2,
        goalTemplate = goalTemplate2,
        maxActions = None,
        verbose = true
      )

      // Run Test
      iterationTest1.run
      iterationTest2.run

      // Gather Performance Data
      avgSuccess += iterationTest1.testMetrics(controllerName).avgGoalCompletion
      avgSuccess += iterationTest2.testMetrics(controllerName).avgGoalCompletion
      avgActions += iterationTest1.testMetrics(controllerName).avgActions
      avgActions += iterationTest2.testMetrics(controllerName).avgActions
      avgRemainingHealth += iterationTest1.testMetrics(controllerName).avgRemainingHealth
      avgRemainingHealth += iterationTest2.testMetrics(controllerName).avgRemainingHealth
      avgRemainingEnergy += iterationTest1.testMetrics(controllerName).avgRemainingEnergy
      avgRemainingEnergy += iterationTest2.testMetrics(controllerName).avgRemainingEnergy

      hAvgSuccess += iterationTest1.testMetrics("Heuristic").avgGoalCompletion
      hAvgSuccess += iterationTest2.testMetrics("Heuristic").avgGoalCompletion
      hAvgActions += iterationTest1.testMetrics("Heuristic").avgActions
      hAvgActions += iterationTest2.testMetrics("Heuristic").avgActions
      hAvgRemainingHealth += iterationTest1.testMetrics("Heuristic").avgRemainingHealth
      hAvgRemainingHealth += iterationTest2.testMetrics("Heuristic").avgRemainingHealth
      hAvgRemainingEnergy += iterationTest1.testMetrics("Heuristic").avgRemainingEnergy
      hAvgRemainingEnergy += iterationTest2.testMetrics("Heuristic").avgRemainingEnergy

      rAvgSuccess += iterationTest1.testMetrics("Random").avgGoalCompletion
      rAvgSuccess += iterationTest2.testMetrics("Random").avgGoalCompletion
      rAvgActions += iterationTest1.testMetrics("Random").avgActions
      rAvgActions += iterationTest2.testMetrics("Random").avgActions
      rAvgRemainingHealth += iterationTest1.testMetrics("Random").avgRemainingHealth
      rAvgRemainingHealth += iterationTest2.testMetrics("Random").avgRemainingHealth
      rAvgRemainingEnergy += iterationTest1.testMetrics("Random").avgRemainingEnergy
      rAvgRemainingEnergy += iterationTest2.testMetrics("Random").avgRemainingEnergy

    }

    // Save Training Performance Data
    val fileName = "Training"
    val filePath = "src/main/scala/testing/Tests/Training/Hybrid/Results/"

    val jsonData = Json.obj(
      ("Random", Json.obj(
        ("Average Successes", Json.fromValues(rAvgSuccess.map(v => Json.fromDoubleOrNull(roundDouble2(v))))),
        ("Average Actions", Json.fromValues(rAvgActions.map(v => Json.fromInt(v)))),
        ("Average Remaining Health", Json.fromValues(rAvgRemainingHealth.map(v => Json.fromDoubleOrNull(roundDouble2(v))))),
        ("Average remainingEnergy", Json.fromValues(rAvgRemainingEnergy.map(v => Json.fromDoubleOrNull(roundDouble2(v)))))
      )),
      ("Heuristic", Json.obj(
        ("Average Successes", Json.fromValues(hAvgSuccess.map(v => Json.fromDoubleOrNull(roundDouble2(v))))),
        ("Average Actions", Json.fromValues(hAvgActions.map(v => Json.fromInt(v)))),
        ("Average Remaining Health", Json.fromValues(hAvgRemainingHealth.map(v => Json.fromDoubleOrNull(roundDouble2(v))))),
        ("Average remainingEnergy", Json.fromValues(hAvgRemainingEnergy.map(v => Json.fromDoubleOrNull(roundDouble2(v)))))
      )),
      (controllerName, Json.obj(
        ("Memory File Name", Json.fromString(memoryFileName)),
        ("Average Successes", Json.fromValues(avgSuccess.map(v => Json.fromDoubleOrNull(roundDouble2(v))))),
        ("Average Actions", Json.fromValues(avgActions.map(v => Json.fromInt(v)))),
        ("Average Remaining Health", Json.fromValues(avgRemainingHealth.map(v => Json.fromDoubleOrNull(roundDouble2(v))))),
        ("Average remainingEnergy", Json.fromValues(avgRemainingEnergy.map(v => Json.fromDoubleOrNull(roundDouble2(v)))))
      ))
    )

    saveJsonFile(fileName, filePath, jsonData)

  }

}
