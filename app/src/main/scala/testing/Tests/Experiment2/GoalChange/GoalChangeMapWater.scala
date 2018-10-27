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


object GoalChangeMapWaterTesting {

  def main(args: Array[String]): Unit = {

    // Testing Setup
    val testTemplate = "EASY"
    val numEnvironments = 200
    val numTestsPerEnvironment = 5
    val weightsSet = BestWeights.hybridLongRun

    // Memory Files
    val fhMemory = "FindHuman"
    val hMemory = "Hybrid"
    val mwMemory = "MapWater"

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
      "FindHuman" -> new FindHumanController(),
      "MapWater" -> new MapWaterController(),
      "SCOUt-MapWater" -> new SCOUtController(mwMemory, "json", false, weightsSet),
      "SCOUt-Hybrid" -> new SCOUtController(hMemory, "json", false, weightsSet),
      "SCOUt-FindHuman" -> new SCOUtController(fhMemory, "json", false, weightsSet))

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

    // Find Human
    val fResults: TestMetric = testingSuite.testMetrics("FindHuman")

    // Map Water
    val mResults: TestMetric = testingSuite.testMetrics("MapWater")

    // SCOUt Map Water
    val smResults: TestMetric = testingSuite.testMetrics("SCOUt-MapWater")

    // SCOUt Hybrid
    val shResults: TestMetric = testingSuite.testMetrics("SCOUt-Hybrid")

    // SCOUt Find Human
    val sfResults: TestMetric = testingSuite.testMetrics("SCOUt-FindHuman")


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
        ("FindHuman", Json.obj(
          ("Test Results", Json.obj(
            ("Goal Completion", Json.fromValues(fResults.runs.map(r => Json.fromDoubleOrNull(r.goalCompletion)))),
            ("Steps", Json.fromValues(fResults.runs.map(r => Json.fromInt(r.steps)))),
            ("Remaining Health", Json.fromValues(fResults.runs.map(r => Json.fromDoubleOrNull(r.remainingHealth)))),
            ("Remaining Energy", Json.fromValues(fResults.runs.map(r => Json.fromDoubleOrNull(r.remainingEnergy))))
          )),
          ("Average Goal Completion", Json.fromDoubleOrNull(fResults.avgGoalCompletion)),
          ("Average Steps", Json.fromInt(fResults.avgActions)),
          ("Average Remaining Health", Json.fromDoubleOrNull(fResults.avgRemainingHealth)),
          ("Average Remaining Energy", Json.fromDoubleOrNull(fResults.avgRemainingEnergy))
        )),
        ("MapWater", Json.obj(
          ("Test Results", Json.obj(
            ("Goal Completion", Json.fromValues(mResults.runs.map(r => Json.fromDoubleOrNull(r.goalCompletion)))),
            ("Steps", Json.fromValues(mResults.runs.map(r => Json.fromInt(r.steps)))),
            ("Remaining Health", Json.fromValues(mResults.runs.map(r => Json.fromDoubleOrNull(r.remainingHealth)))),
            ("Remaining Energy", Json.fromValues(mResults.runs.map(r => Json.fromDoubleOrNull(r.remainingEnergy))))
          )),
          ("Average Goal Completion", Json.fromDoubleOrNull(mResults.avgGoalCompletion)),
          ("Average Steps", Json.fromInt(mResults.avgActions)),
          ("Average Remaining Health", Json.fromDoubleOrNull(mResults.avgRemainingHealth)),
          ("Average Remaining Energy", Json.fromDoubleOrNull(mResults.avgRemainingEnergy))
        )),
        ("SCOUt-MapWater", Json.obj(
          ("Test Results", Json.obj(
            ("Goal Completion", Json.fromValues(smResults.runs.map(r => Json.fromDoubleOrNull(r.goalCompletion)))),
            ("Steps", Json.fromValues(smResults.runs.map(r => Json.fromInt(r.steps)))),
            ("Remaining Health", Json.fromValues(smResults.runs.map(r => Json.fromDoubleOrNull(r.remainingHealth)))),
            ("Remaining Energy", Json.fromValues(smResults.runs.map(r => Json.fromDoubleOrNull(r.remainingEnergy))))
          )),
          ("Average Goal Completion", Json.fromDoubleOrNull(smResults.avgGoalCompletion)),
          ("Average Steps", Json.fromInt(smResults.avgActions)),
          ("Average Remaining Health", Json.fromDoubleOrNull(smResults.avgRemainingHealth)),
          ("Average Remaining Energy", Json.fromDoubleOrNull(smResults.avgRemainingEnergy))
        )),
        ("SCOUt-Hybrid", Json.obj(
          ("Test Results", Json.obj(
            ("Goal Completion", Json.fromValues(shResults.runs.map(r => Json.fromDoubleOrNull(r.goalCompletion)))),
            ("Steps", Json.fromValues(shResults.runs.map(r => Json.fromInt(r.steps)))),
            ("Remaining Health", Json.fromValues(shResults.runs.map(r => Json.fromDoubleOrNull(r.remainingHealth)))),
            ("Remaining Energy", Json.fromValues(shResults.runs.map(r => Json.fromDoubleOrNull(r.remainingEnergy))))
          )),
          ("Average Goal Completion", Json.fromDoubleOrNull(shResults.avgGoalCompletion)),
          ("Average Steps", Json.fromInt(shResults.avgActions)),
          ("Average Remaining Health", Json.fromDoubleOrNull(shResults.avgRemainingHealth)),
          ("Average Remaining Energy", Json.fromDoubleOrNull(shResults.avgRemainingEnergy))
        )),
        ("SCOUt-FindHuman", Json.obj(
          ("Test Results", Json.obj(
            ("Goal Completion", Json.fromValues(sfResults.runs.map(r => Json.fromDoubleOrNull(r.goalCompletion)))),
            ("Steps", Json.fromValues(sfResults.runs.map(r => Json.fromInt(r.steps)))),
            ("Remaining Health", Json.fromValues(sfResults.runs.map(r => Json.fromDoubleOrNull(r.remainingHealth)))),
            ("Remaining Energy", Json.fromValues(sfResults.runs.map(r => Json.fromDoubleOrNull(r.remainingEnergy))))
          )),
          ("Average Goal Completion", Json.fromDoubleOrNull(sfResults.avgGoalCompletion)),
          ("Average Steps", Json.fromInt(sfResults.avgActions)),
          ("Average Remaining Health", Json.fromDoubleOrNull(sfResults.avgRemainingHealth)),
          ("Average Remaining Energy", Json.fromDoubleOrNull(sfResults.avgRemainingEnergy))
        ))
      ))
    )

    val fileName = "MapWater-Test-Results-" + testTemplate
    val filePath = "src/main/scala/testing/Tests/Experiment2/GoalChange/Results/"
    saveJsonFile(fileName, filePath, jsonData)

  }

}
