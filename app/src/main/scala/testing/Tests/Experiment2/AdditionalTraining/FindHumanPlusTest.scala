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


object FindHumanPlusTesting {

  def main(args: Array[String]): Unit = {

    // Testing Setup
    val testTemplate = "HARD"
    val numEnvironments = 200
    val numTestsPerEnvironment = 5

    val memoryFileName = "FindHumanPlus"
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
      "SCOUt-MW" -> new SCOUtController("MapWater", "json", false, weightsSet),
      "SCOUt-FH" -> new SCOUtController("FindHuman", "json", false, weightsSet),
      "SCOUt-FHP" -> new SCOUtController(memoryFileName, "json", false, weightsSet))

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
    val smwResults: TestMetric = testingSuite.testMetrics("SCOUt-MW")

    // SCOUt
    val sfhResults: TestMetric = testingSuite.testMetrics("SCOUt-FH")

    // SCOUt
    val sfhpResults: TestMetric = testingSuite.testMetrics("SCOUt-FHP")

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
        ("SCOUt Map Water", Json.obj(
          ("Test Results", Json.obj(
            ("Goal Completion", Json.fromValues(smwResults.runs.map(r => Json.fromDoubleOrNull(r.goalCompletion)))),
            ("Steps", Json.fromValues(smwResults.runs.map(r => Json.fromInt(r.steps)))),
            ("Remaining Health", Json.fromValues(smwResults.runs.map(r => Json.fromDoubleOrNull(r.remainingHealth)))),
            ("Remaining Energy", Json.fromValues(smwResults.runs.map(r => Json.fromDoubleOrNull(r.remainingEnergy))))
          )),
          ("Average Goal Completion", Json.fromDoubleOrNull(smwResults.avgGoalCompletion)),
          ("Average Steps", Json.fromInt(smwResults.avgActions)),
          ("Average Remaining Health", Json.fromDoubleOrNull(smwResults.avgRemainingHealth)),
          ("Average Remaining Energy", Json.fromDoubleOrNull(smwResults.avgRemainingEnergy))
        )),
        ("SCOUt Find Human", Json.obj(
          ("Test Results", Json.obj(
            ("Goal Completion", Json.fromValues(sfhResults.runs.map(r => Json.fromDoubleOrNull(r.goalCompletion)))),
            ("Steps", Json.fromValues(sfhResults.runs.map(r => Json.fromInt(r.steps)))),
            ("Remaining Health", Json.fromValues(sfhResults.runs.map(r => Json.fromDoubleOrNull(r.remainingHealth)))),
            ("Remaining Energy", Json.fromValues(sfhResults.runs.map(r => Json.fromDoubleOrNull(r.remainingEnergy))))
          )),
          ("Average Goal Completion", Json.fromDoubleOrNull(sfhResults.avgGoalCompletion)),
          ("Average Steps", Json.fromInt(sfhResults.avgActions)),
          ("Average Remaining Health", Json.fromDoubleOrNull(sfhResults.avgRemainingHealth)),
          ("Average Remaining Energy", Json.fromDoubleOrNull(sfhResults.avgRemainingEnergy))
        )),
        ("SCOUt Find Human Plus", Json.obj(
          ("Test Results", Json.obj(
            ("Goal Completion", Json.fromValues(sfhpResults.runs.map(r => Json.fromDoubleOrNull(r.goalCompletion)))),
            ("Steps", Json.fromValues(sfhpResults.runs.map(r => Json.fromInt(r.steps)))),
            ("Remaining Health", Json.fromValues(sfhpResults.runs.map(r => Json.fromDoubleOrNull(r.remainingHealth)))),
            ("Remaining Energy", Json.fromValues(sfhpResults.runs.map(r => Json.fromDoubleOrNull(r.remainingEnergy))))
          )),
          ("Average Goal Completion", Json.fromDoubleOrNull(sfhpResults.avgGoalCompletion)),
          ("Average Steps", Json.fromInt(sfhpResults.avgActions)),
          ("Average Remaining Health", Json.fromDoubleOrNull(sfhpResults.avgRemainingHealth)),
          ("Average Remaining Energy", Json.fromDoubleOrNull(sfhpResults.avgRemainingEnergy))
        ))
      ))
    )

    val fileName = "Test-Results-" + memoryFileName + "-" + testTemplate
    val filePath = "src/main/scala/testing/Tests/Experiment2/AdditionalTraining/Results/"
    saveJsonFile(fileName, filePath, jsonData)

  }

}
