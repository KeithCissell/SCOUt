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


object SensorChangeFindHumanTesting {

  def main(args: Array[String]): Unit = {

    // Testing Setup
    val testTemplate = "EASY"
    val numEnvironments = 200
    val numTestsPerEnvironment = 5

    val memoryFileName = "FindHuman"
    val weightsSet = BestWeights.hybridLongRun

    val agentSensors = List(
      new ElevationSensor(false),
      new DecibelSensor(true),
      new TemperatureSensor(true),
      new WaterSensor(false))

    val testEnvironments: Map[String,Int] = Map()
    val testTemplates = Map(
      testTemplate -> (numEnvironments, numTestsPerEnvironment)
    )

    val goalTemplate = new FindAnomaliesTemplate(Map("Human" -> 1), None)

    val controllers = Map(
      "Random" -> new RandomController(),
      "FindHuman" -> new FindHumanController(),
      "SCOUt-AllSensors" -> new SCOUtController(memoryFileName, "json", false, weightsSet),
      "SCOUt-NoElevationSensor" -> new SCOUtController(memoryFileName, "json", false, weightsSet),
      "SCOUt-NoWaterSensor" -> new SCOUtController(memoryFileName, "json", false, weightsSet),
      "SCOUt-NoTemperatureSensor" -> new SCOUtController(memoryFileName, "json", false, weightsSet),
      "SCOUt-NoDecibelSensor" -> new SCOUtController(memoryFileName, "json", false, weightsSet))


    val agentSensorLists = Map(
      "SCOUt-NoElevationSensor" -> List(
        new DecibelSensor(false),
        new TemperatureSensor(false),
        new WaterSensor(true)),
      "SCOUt-NoWaterSensor" -> List(
        new ElevationSensor(false),
        new DecibelSensor(false),
        new TemperatureSensor(false)),
      "SCOUt-NoTemperatureSensor" -> List(
        new ElevationSensor(false),
        new DecibelSensor(false),
        new WaterSensor(true)),
      "SCOUt-NoDecibelSensor" -> List(
        new ElevationSensor(false),
        new TemperatureSensor(false),
        new WaterSensor(true))
    )


    // Test Controllers
    val testingSuite = new Test(
      testEnvironments = testEnvironments,
      testTemplates = testTemplates,
      controllers = controllers,
      sensors = agentSensors,
      goalTemplate = goalTemplate,
      maxActions = None,
      verbose = true,
      sensorLists = agentSensorLists
    )

    testingSuite.run


    // Collect and Save Test Metrics
    // Random
    val rResults: TestMetric = testingSuite.testMetrics("Random")

    // Find Human
    val fResults: TestMetric = testingSuite.testMetrics("FindHuman")

    // SCOUt All Sensors
    val aResults: TestMetric = testingSuite.testMetrics("SCOUt-AllSensors")

    // SCOUt No Elevation Sensor
    val eResults: TestMetric = testingSuite.testMetrics("SCOUt-NoElevationSensor")

    // SCOUt No Water Sensor
    val wResults: TestMetric = testingSuite.testMetrics("SCOUt-NoWaterSensor")

    // SCOUt No Temperature Sensor
    val tResults: TestMetric = testingSuite.testMetrics("SCOUt-NoTemperatureSensor")

    // SCOUt No Decibel Sensor
    val dResults: TestMetric = testingSuite.testMetrics("SCOUt-NoDecibelSensor")


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
        ("SCOUt-AllSensors", Json.obj(
          ("Test Results", Json.obj(
            ("Goal Completion", Json.fromValues(aResults.runs.map(r => Json.fromDoubleOrNull(r.goalCompletion)))),
            ("Steps", Json.fromValues(aResults.runs.map(r => Json.fromInt(r.steps)))),
            ("Remaining Health", Json.fromValues(aResults.runs.map(r => Json.fromDoubleOrNull(r.remainingHealth)))),
            ("Remaining Energy", Json.fromValues(aResults.runs.map(r => Json.fromDoubleOrNull(r.remainingEnergy))))
          )),
          ("Average Goal Completion", Json.fromDoubleOrNull(aResults.avgGoalCompletion)),
          ("Average Steps", Json.fromInt(aResults.avgActions)),
          ("Average Remaining Health", Json.fromDoubleOrNull(aResults.avgRemainingHealth)),
          ("Average Remaining Energy", Json.fromDoubleOrNull(aResults.avgRemainingEnergy))
        )),
        ("SCOUt-NoElevationSensor", Json.obj(
          ("Test Results", Json.obj(
            ("Goal Completion", Json.fromValues(eResults.runs.map(r => Json.fromDoubleOrNull(r.goalCompletion)))),
            ("Steps", Json.fromValues(eResults.runs.map(r => Json.fromInt(r.steps)))),
            ("Remaining Health", Json.fromValues(eResults.runs.map(r => Json.fromDoubleOrNull(r.remainingHealth)))),
            ("Remaining Energy", Json.fromValues(eResults.runs.map(r => Json.fromDoubleOrNull(r.remainingEnergy))))
          )),
          ("Average Goal Completion", Json.fromDoubleOrNull(eResults.avgGoalCompletion)),
          ("Average Steps", Json.fromInt(eResults.avgActions)),
          ("Average Remaining Health", Json.fromDoubleOrNull(eResults.avgRemainingHealth)),
          ("Average Remaining Energy", Json.fromDoubleOrNull(eResults.avgRemainingEnergy))
        )),
        ("SCOUt-NoWaterSensor", Json.obj(
          ("Test Results", Json.obj(
            ("Goal Completion", Json.fromValues(wResults.runs.map(r => Json.fromDoubleOrNull(r.goalCompletion)))),
            ("Steps", Json.fromValues(wResults.runs.map(r => Json.fromInt(r.steps)))),
            ("Remaining Health", Json.fromValues(wResults.runs.map(r => Json.fromDoubleOrNull(r.remainingHealth)))),
            ("Remaining Energy", Json.fromValues(wResults.runs.map(r => Json.fromDoubleOrNull(r.remainingEnergy))))
          )),
          ("Average Goal Completion", Json.fromDoubleOrNull(wResults.avgGoalCompletion)),
          ("Average Steps", Json.fromInt(wResults.avgActions)),
          ("Average Remaining Health", Json.fromDoubleOrNull(wResults.avgRemainingHealth)),
          ("Average Remaining Energy", Json.fromDoubleOrNull(wResults.avgRemainingEnergy))
        )),
        ("SCOUt-NoTemperatureSensor", Json.obj(
          ("Test Results", Json.obj(
            ("Goal Completion", Json.fromValues(tResults.runs.map(r => Json.fromDoubleOrNull(r.goalCompletion)))),
            ("Steps", Json.fromValues(tResults.runs.map(r => Json.fromInt(r.steps)))),
            ("Remaining Health", Json.fromValues(tResults.runs.map(r => Json.fromDoubleOrNull(r.remainingHealth)))),
            ("Remaining Energy", Json.fromValues(tResults.runs.map(r => Json.fromDoubleOrNull(r.remainingEnergy))))
          )),
          ("Average Goal Completion", Json.fromDoubleOrNull(tResults.avgGoalCompletion)),
          ("Average Steps", Json.fromInt(tResults.avgActions)),
          ("Average Remaining Health", Json.fromDoubleOrNull(tResults.avgRemainingHealth)),
          ("Average Remaining Energy", Json.fromDoubleOrNull(tResults.avgRemainingEnergy))
        )),
        ("SCOUt-NoDecibelSensor", Json.obj(
          ("Test Results", Json.obj(
            ("Goal Completion", Json.fromValues(dResults.runs.map(r => Json.fromDoubleOrNull(r.goalCompletion)))),
            ("Steps", Json.fromValues(dResults.runs.map(r => Json.fromInt(r.steps)))),
            ("Remaining Health", Json.fromValues(dResults.runs.map(r => Json.fromDoubleOrNull(r.remainingHealth)))),
            ("Remaining Energy", Json.fromValues(dResults.runs.map(r => Json.fromDoubleOrNull(r.remainingEnergy))))
          )),
          ("Average Goal Completion", Json.fromDoubleOrNull(dResults.avgGoalCompletion)),
          ("Average Steps", Json.fromInt(dResults.avgActions)),
          ("Average Remaining Health", Json.fromDoubleOrNull(dResults.avgRemainingHealth)),
          ("Average Remaining Energy", Json.fromDoubleOrNull(dResults.avgRemainingEnergy))
        ))
      ))
    )

    val fileName = "FindHuman-Test-Results-" + testTemplate
    val filePath = "src/main/scala/testing/Tests/Experiment2/SensorChange/Results/"
    saveJsonFile(fileName, filePath, jsonData)

  }

}
