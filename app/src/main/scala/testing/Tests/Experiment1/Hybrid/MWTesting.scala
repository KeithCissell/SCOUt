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


object HybridMapWaterTesting {

  def main(args: Array[String]): Unit = {

    // Testing Setup
    val testTemplate = "EASY"
    val numEnvironments = 200
    val numTestsPerEnvironment = 5

    val memoryFileName = "Hybrid"
    val weightsSet = BestWeights.hybridLongRun

    val agentSensors = List(
      new ElevationSensor(false),
      new WaterSensor(true))

    val testEnvironments: Map[String,Int] = Map()
    val testTemplates = Map(
      testTemplate -> (numEnvironments, numTestsPerEnvironment)
    )

    val goalTemplate = new MapElementsTemplate(List("Water Depth"), None)

    val controllers = Map(
      "Random" -> new RandomController(),
      "Heuristic" -> new FindHumanController(),
      "SCOUt" -> new SCOUtController(memoryFileName, "json", false, weightsSet))

    // Test Controllers
    val testingSuite = new Test(
      testEnvironments = testEnvironments,
      testTemplates = testTemplates,
      controllers = controllers,
      sensors = agentSensors,
      goalTemplate = goalTemplate,
      maxActions = None,
      verbose = true
    )

    testingSuite.run


    // Collect and Save Test Metrics
    // Random
    val rResults: TestMetric = testingSuite.testMetrics("Random")

    // Heuristic
    val hResults: TestMetric = testingSuite.testMetrics("Heuristic")

    // SCOUt
    val sResults: TestMetric = testingSuite.testMetrics("SCOUt")


    val jsonData = Json.obj(
      (testTemplate, Json.obj(
        ("Number of Tests", Json.fromInt(numEnvironments * numTestsPerEnvironment)),
        ("Number of Environments Generated", Json.fromInt(numEnvironments)),
        ("Number of Tests Per Environment", Json.fromInt(numTestsPerEnvironment)),
        ("Random", Json.obj(
          ("Test Results", Json.obj(
            ("Goal Completion", Json.fromValues(rResults.runs.map(r => Json.fromDoubleOrNull(r.goalCompletion)))),
            ("Steps", Json.fromValues(rResults.runs.map(r => Json.fromInt(r.steps)))),
            ("Remaining Health", Json.fromValues(rResults.runs.map(r => Json.fromDoubleOrNull(r.remainingHealth)))),
            ("Remaining Energy", Json.fromValues(rResults.runs.map(r => Json.fromDoubleOrNull(r.remainingEnergy))))
          )),
          ("Average Goal Completion", Json.fromDoubleOrNull(rResults.avgGoalCompletion)),
          ("Average Steps", Json.fromInt(rResults.avgActions)),
          ("Average Remaining Health", Json.fromDoubleOrNull(rResults.avgRemainingHealth)),
          ("Average Remaining Energy", Json.fromDoubleOrNull(rResults.avgRemainingEnergy))
        )),
        ("Heuristic", Json.obj(
          ("Test Results", Json.obj(
            ("Goal Completion", Json.fromValues(hResults.runs.map(r => Json.fromDoubleOrNull(r.goalCompletion)))),
            ("Steps", Json.fromValues(hResults.runs.map(r => Json.fromInt(r.steps)))),
            ("Remaining Health", Json.fromValues(hResults.runs.map(r => Json.fromDoubleOrNull(r.remainingHealth)))),
            ("Remaining Energy", Json.fromValues(hResults.runs.map(r => Json.fromDoubleOrNull(r.remainingEnergy))))
          )),
          ("Average Goal Completion", Json.fromDoubleOrNull(hResults.avgGoalCompletion)),
          ("Average Steps", Json.fromInt(hResults.avgActions)),
          ("Average Remaining Health", Json.fromDoubleOrNull(hResults.avgRemainingHealth)),
          ("Average Remaining Energy", Json.fromDoubleOrNull(hResults.avgRemainingEnergy))
        )),
        ("SCOUt", Json.obj(
          ("Test Results", Json.obj(
            ("Goal Completion", Json.fromValues(sResults.runs.map(r => Json.fromDoubleOrNull(r.goalCompletion)))),
            ("Steps", Json.fromValues(sResults.runs.map(r => Json.fromInt(r.steps)))),
            ("Remaining Health", Json.fromValues(sResults.runs.map(r => Json.fromDoubleOrNull(r.remainingHealth)))),
            ("Remaining Energy", Json.fromValues(sResults.runs.map(r => Json.fromDoubleOrNull(r.remainingEnergy))))
          )),
          ("Average Goal Completion", Json.fromDoubleOrNull(sResults.avgGoalCompletion)),
          ("Average Steps", Json.fromInt(sResults.avgActions)),
          ("Average Remaining Health", Json.fromDoubleOrNull(sResults.avgRemainingHealth)),
          ("Average Remaining Energy", Json.fromDoubleOrNull(sResults.avgRemainingEnergy))
        ))
      ))
    )

    val fileName = "MapWater-Test-Results-" + testTemplate
    val filePath = "src/main/scala/testing/Tests/Experiment1/Hybrid/Results/"
    saveJsonFile(fileName, filePath, jsonData)

  }

}
