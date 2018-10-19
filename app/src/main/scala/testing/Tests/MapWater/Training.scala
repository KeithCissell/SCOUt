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
import weighttuning.WeightTuning._
import scala.collection.mutable.{Map => MutableMap}
import scala.collection.mutable.{ArrayBuffer => AB}


object MapWaterTraining {

  def main(args: Array[String]): Unit = {

    // Training Setup
    val trainingIterations = 50
    val testingFrequency = 1
    val controllerName = "SCOUt"
    val memoryFileName = "MapWaterTEST1"

    val agentSensors = List(
      new ElevationSensor(false),
      // new DecibelSensor(false),
      new TemperatureSensor(false),
      new WaterSensor(true))

    val goalTemplate = new MapElementsTemplate(List("Water Depth"), None)

    // Weight Set
    val handTuned = new Individual(weights = List(
      0.0,
      0.0,
      1.0,
      1.0,
      4.0,
      1.0,
      5.0,
      2.0,
      4.0,
      1.0,
      1.0,
      2.0,
      3.0,
      1.0,
      1.0,
      1.0,
      1.0,
      1.0,
      1.0,
      0.5,
      0.4,
      10.0
    ))
    val gaResult = new Individual(weights = List(
      0.73,
      0.18,
      0.88,
      0.83,
      1.0,
      0.68,
      0.15,
      0.74,
      0.6,
      0.33,
      0.98,
      0.11,
      0.97,
      0.22,
      0.65,
      0.67,
      0.71,
      0.57,
      1.0,
      0.41,
      1.0,
      21.73
    ))

    // Training Performance Data
    var avgSuccess: AB[Double] = AB()
    var avgActions: AB[Int] = AB()
    var avgRemainingHealth: AB[Double] = AB()
    var avgRemainingEnergy: AB[Double] = AB()

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
      val training = new Test(
        testEnvironments = Map(),
        testTemplates = Map(
          // "10by10NoMods" -> (1, 1),
          "BeginnerCourse" -> (1, 1)),
        controllers = Map(
          controllerName -> new SCOUtController(memoryFileName, "json", true, Some(handTuned.generateWeightsSet))),
        sensors = agentSensors,
        goalTemplate = goalTemplate,
        maxActions = None
      )

      training.run

      // Test Controller
      println()
      println()
      println(s"********** TEST ${i+1} ***********")
      val iterationTest = new Test(
        testEnvironments = Map(),
        testTemplates = Map(
          // "10by10NoMods" -> (15, 1),
          "BeginnerCourse" -> (15, 1)),
        controllers = Map(
          "Random" -> new RandomController(),
          controllerName -> new SCOUtController(memoryFileName, "json", false, None)),
        sensors = agentSensors,
        goalTemplate = goalTemplate,
        maxActions = None,
        verbose = true
      )

      // Run Test
      iterationTest.run

      // Gather Performance Data
      avgSuccess += iterationTest.testMetrics(controllerName).avgGoalCompletion
      avgActions += iterationTest.testMetrics(controllerName).avgActions
      avgRemainingHealth += iterationTest.testMetrics(controllerName).avgRemainingHealth
      avgRemainingEnergy += iterationTest.testMetrics(controllerName).avgRemainingEnergy
      rAvgSuccess += iterationTest.testMetrics("Random").avgGoalCompletion
      rAvgActions += iterationTest.testMetrics("Random").avgActions
      rAvgRemainingHealth += iterationTest.testMetrics("Random").avgRemainingHealth
      rAvgRemainingEnergy += iterationTest.testMetrics("Random").avgRemainingEnergy

    }

    // Save Training Performance Data
    val fileName = controllerName + memoryFileName

    val jsonData = Json.obj(
      (controllerName, Json.obj(
        ("Memory File Name", Json.fromString(memoryFileName)),
        ("Average Successes", Json.fromValues(avgSuccess.map(v => Json.fromDoubleOrNull(roundDouble2(v))))),
        ("Average Actions", Json.fromValues(avgActions.map(v => Json.fromInt(v)))),
        ("Average Remaining Health", Json.fromValues(avgRemainingHealth.map(v => Json.fromDoubleOrNull(roundDouble2(v))))),
        ("Average remainingEnergy", Json.fromValues(avgRemainingEnergy.map(v => Json.fromDoubleOrNull(roundDouble2(v)))))
      )),
      ("Random", Json.obj(
        ("Average Successes", Json.fromValues(rAvgSuccess.map(v => Json.fromDoubleOrNull(roundDouble2(v))))),
        ("Average Actions", Json.fromValues(rAvgActions.map(v => Json.fromInt(v)))),
        ("Average Remaining Health", Json.fromValues(rAvgRemainingHealth.map(v => Json.fromDoubleOrNull(roundDouble2(v))))),
        ("Average remainingEnergy", Json.fromValues(rAvgRemainingEnergy.map(v => Json.fromDoubleOrNull(roundDouble2(v)))))
      ))
    )

    saveJsonFile(fileName, trainingPerformanceDataPath, jsonData)

  }

}
