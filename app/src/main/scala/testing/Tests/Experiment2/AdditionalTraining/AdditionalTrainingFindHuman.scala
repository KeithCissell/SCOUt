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


object AdditionalTrainingFindHuman {

  def main(args: Array[String]): Unit = {

    // Training Setup
    val trainingIterations = 30
    val controllerName = "SCOUt-FindHuman+"
    val weightsSet = BestWeights.hybridLongRun

    // Memory Files
    val memoryFileName = "FindHumanPlus"
    val fhMemory = "FindHuman"
    val mwMemory = "MapWater"

    val agentSensors = List(
      new ElevationSensor(false),
      new WaterSensor(false))

    val testEnvironments: Map[String,Int] = Map()
    val testTemplates: Map[String,(Int,Int)] = Map(
      "EASY" -> (20, 1),
      "MEDIUM" -> (20, 1),
      "HARD" -> (20, 1)
    )

    val trainingEnvironments: Map[String,Int] = Map()
    val trainingTemplates = Map(
      "EASY" -> (1, 1),
      "MEDIUM" -> (1, 1),
      "HARD" -> (1, 1)
    )

    val goalTemplate = new MapElementsTemplate(List("Water Depth"), None)

    // Training Performance Data
    var avgSuccess: AB[Double] = AB()
    var avgActions: AB[Int] = AB()
    var avgRemainingHealth: AB[Double] = AB()
    var avgRemainingEnergy: AB[Double] = AB()

    var ogAvgSuccess: AB[Double] = AB()
    var ogAvgActions: AB[Int] = AB()
    var ogAvgRemainingHealth: AB[Double] = AB()
    var ogAvgRemainingEnergy: AB[Double] = AB()

    var fhAvgSuccess: AB[Double] = AB()
    var fhAvgActions: AB[Int] = AB()
    var fhAvgRemainingHealth: AB[Double] = AB()
    var fhAvgRemainingEnergy: AB[Double] = AB()

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
      val training = new Test(
        testEnvironments = trainingEnvironments,
        testTemplates = trainingTemplates,
        controllers = Map(
          controllerName -> new SCOUtController(memoryFileName, "json", true, weightsSet)),
        sensors = agentSensors,
        goalTemplate = goalTemplate,
        maxActions = None
      )

      training.run

      // Test Controller
      println()
      println()
      println(s"********** TESTING ${i+1} ***********")
      val iterationTest = new Test(
        testEnvironments = testEnvironments,
        testTemplates = testTemplates,
        controllers = Map(
          "Random" -> new RandomController(),
          "Heuristic" -> new FindHumanController(),
          "SCOUt-MapWater" -> new SCOUtController(mwMemory, "json", false, weightsSet),
          "SCOUt-FinHuman" -> new SCOUtController(fhMemory, "json", false, weightsSet),
          controllerName -> new SCOUtController(memoryFileName, "json", false, weightsSet)),
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

      ogAvgSuccess += iterationTest.testMetrics("SCOUt-FinHuman").avgGoalCompletion
      ogAvgActions += iterationTest.testMetrics("SCOUt-FinHuman").avgActions
      ogAvgRemainingHealth += iterationTest.testMetrics("SCOUt-FinHuman").avgRemainingHealth
      ogAvgRemainingEnergy += iterationTest.testMetrics("SCOUt-FinHuman").avgRemainingEnergy

      mwAvgSuccess += iterationTest.testMetrics("SCOUt-MapWaterMapWater").avgGoalCompletion
      mwAvgActions += iterationTest.testMetrics("SCOUt-MapWater").avgActions
      mwAvgRemainingHealth += iterationTest.testMetrics("SCOUt-MapWater").avgRemainingHealth
      mwAvgRemainingEnergy += iterationTest.testMetrics("SCOUt-MapWater").avgRemainingEnergy

      hAvgSuccess += iterationTest.testMetrics("Heuristic").avgGoalCompletion
      hAvgActions += iterationTest.testMetrics("Heuristic").avgActions
      hAvgRemainingHealth += iterationTest.testMetrics("Heuristic").avgRemainingHealth
      hAvgRemainingEnergy += iterationTest.testMetrics("Heuristic").avgRemainingEnergy

      rAvgSuccess += iterationTest.testMetrics("Random").avgGoalCompletion
      rAvgActions += iterationTest.testMetrics("Random").avgActions
      rAvgRemainingHealth += iterationTest.testMetrics("Random").avgRemainingHealth
      rAvgRemainingEnergy += iterationTest.testMetrics("Random").avgRemainingEnergy

    }

    // Save Training Performance Data
    val fileName = "AdditionalTraining-FindHuman"
    val filePath = "src/main/scala/testing/Tests/Experiment2/AdditionalTraining/Results/"

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
      ("SCOUt-MapWater", Json.obj(
        ("Average Successes", Json.fromValues(mwAvgSuccess.map(v => Json.fromDoubleOrNull(roundDouble2(v))))),
        ("Average Actions", Json.fromValues(mwAvgActions.map(v => Json.fromInt(v)))),
        ("Average Remaining Health", Json.fromValues(mwAvgRemainingHealth.map(v => Json.fromDoubleOrNull(roundDouble2(v))))),
        ("Average remainingEnergy", Json.fromValues(mwAvgRemainingEnergy.map(v => Json.fromDoubleOrNull(roundDouble2(v)))))
      )),
      ("SCOUt-FindHuman", Json.obj(
        ("Average Successes", Json.fromValues(ogAvgSuccess.map(v => Json.fromDoubleOrNull(roundDouble2(v))))),
        ("Average Actions", Json.fromValues(ogAvgActions.map(v => Json.fromInt(v)))),
        ("Average Remaining Health", Json.fromValues(ogAvgRemainingHealth.map(v => Json.fromDoubleOrNull(roundDouble2(v))))),
        ("Average remainingEnergy", Json.fromValues(ogAvgRemainingEnergy.map(v => Json.fromDoubleOrNull(roundDouble2(v)))))
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
