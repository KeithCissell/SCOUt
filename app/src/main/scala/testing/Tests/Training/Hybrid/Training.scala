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
    val trainingIterations = 120
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

    val goalTemplate1 = new FindAnomaliesTemplate(Map("Human" -> 1), None)
    val goalTemplate2 = new MapElementsTemplate(List("Water Depth"), None)

    // Training Performance Data
    // Find Human
    var avgSuccessFH: AB[Double] = AB()
    var avgActionsFH: AB[Int] = AB()
    var avgRemainingHealthFH: AB[Double] = AB()
    var avgRemainingEnergyFH: AB[Double] = AB()

    var hAvgSuccessFH: AB[Double] = AB()
    var hAvgActionsFH: AB[Int] = AB()
    var hAvgRemainingHealthFH: AB[Double] = AB()
    var hAvgRemainingEnergyFH: AB[Double] = AB()

    var rAvgSuccessFH: AB[Double] = AB()
    var rAvgActionsFH: AB[Int] = AB()
    var rAvgRemainingHealthFH: AB[Double] = AB()
    var rAvgRemainingEnergyFH: AB[Double] = AB()

    // Map Water
    var avgSuccessMW: AB[Double] = AB()
    var avgActionsMW: AB[Int] = AB()
    var avgRemainingHealthMW: AB[Double] = AB()
    var avgRemainingEnergyMW: AB[Double] = AB()

    var hAvgSuccessMW: AB[Double] = AB()
    var hAvgActionsMW: AB[Int] = AB()
    var hAvgRemainingHealthMW: AB[Double] = AB()
    var hAvgRemainingEnergyMW: AB[Double] = AB()

    var rAvgSuccessMW: AB[Double] = AB()
    var rAvgActionsMW: AB[Int] = AB()
    var rAvgRemainingHealthMW: AB[Double] = AB()
    var rAvgRemainingEnergyMW: AB[Double] = AB()

    // TRAINING LOOP
    for (i <- 0 until trainingIterations) {

      // Run a Training Test
      println()
      println()
      println(s"********** TRAINING ${i+1} ***********")
      val trainingSensors = if (i % 2 == 0) agentSensors1 else agentSensors2
      val trainingGoal = if (i % 2 == 0) goalTemplate1 else goalTemplate2
      val templateName = i match {
        case i if (i > trainingIterations * 2 / 3) => "HARD"
        case i if (i > trainingIterations / 3)     => "MEDIUM"
        case _                                     => "EASY"
      }
      val training = new Test(
        testEnvironments = Map(),
        testTemplates = Map(templateName -> (1, 1)),
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
      avgSuccessFH += iterationTest1.testMetrics(controllerName).avgGoalCompletion
      avgSuccessMW += iterationTest2.testMetrics(controllerName).avgGoalCompletion
      avgActionsFH += iterationTest1.testMetrics(controllerName).avgActions
      avgActionsMW += iterationTest2.testMetrics(controllerName).avgActions
      avgRemainingHealthFH += iterationTest1.testMetrics(controllerName).avgRemainingHealth
      avgRemainingHealthMW += iterationTest2.testMetrics(controllerName).avgRemainingHealth
      avgRemainingEnergyFH += iterationTest1.testMetrics(controllerName).avgRemainingEnergy
      avgRemainingEnergyMW += iterationTest2.testMetrics(controllerName).avgRemainingEnergy

      hAvgSuccessFH += iterationTest1.testMetrics("Heuristic").avgGoalCompletion
      hAvgSuccessMW += iterationTest2.testMetrics("Heuristic").avgGoalCompletion
      hAvgActionsFH += iterationTest1.testMetrics("Heuristic").avgActions
      hAvgActionsMW += iterationTest2.testMetrics("Heuristic").avgActions
      hAvgRemainingHealthFH += iterationTest1.testMetrics("Heuristic").avgRemainingHealth
      hAvgRemainingHealthMW += iterationTest2.testMetrics("Heuristic").avgRemainingHealth
      hAvgRemainingEnergyFH += iterationTest1.testMetrics("Heuristic").avgRemainingEnergy
      hAvgRemainingEnergyMW += iterationTest2.testMetrics("Heuristic").avgRemainingEnergy

      rAvgSuccessFH += iterationTest1.testMetrics("Random").avgGoalCompletion
      rAvgSuccessMW += iterationTest2.testMetrics("Random").avgGoalCompletion
      rAvgActionsFH += iterationTest1.testMetrics("Random").avgActions
      rAvgActionsMW += iterationTest2.testMetrics("Random").avgActions
      rAvgRemainingHealthFH += iterationTest1.testMetrics("Random").avgRemainingHealth
      rAvgRemainingHealthMW += iterationTest2.testMetrics("Random").avgRemainingHealth
      rAvgRemainingEnergyFH += iterationTest1.testMetrics("Random").avgRemainingEnergy
      rAvgRemainingEnergyMW += iterationTest2.testMetrics("Random").avgRemainingEnergy

    }

    // Save Training Performance Data
    val fileName = "Training"
    val filePath = "src/main/scala/testing/Tests/Training/Hybrid/Results/"

    val jsonData = Json.obj(
      ("Find Human", Json.obj(
        ("Random", Json.obj(
          ("Average Successes", Json.fromValues(rAvgSuccessFH.map(v => Json.fromDoubleOrNull(roundDouble2(v))))),
          ("Average Actions", Json.fromValues(rAvgActionsFH.map(v => Json.fromInt(v)))),
          ("Average Remaining Health", Json.fromValues(rAvgRemainingHealthFH.map(v => Json.fromDoubleOrNull(roundDouble2(v))))),
          ("Average remainingEnergy", Json.fromValues(rAvgRemainingEnergyFH.map(v => Json.fromDoubleOrNull(roundDouble2(v)))))
        )),
        ("Heuristic", Json.obj(
          ("Average Successes", Json.fromValues(hAvgSuccessFH.map(v => Json.fromDoubleOrNull(roundDouble2(v))))),
          ("Average Actions", Json.fromValues(hAvgActionsFH.map(v => Json.fromInt(v)))),
          ("Average Remaining Health", Json.fromValues(hAvgRemainingHealthFH.map(v => Json.fromDoubleOrNull(roundDouble2(v))))),
          ("Average remainingEnergy", Json.fromValues(hAvgRemainingEnergyFH.map(v => Json.fromDoubleOrNull(roundDouble2(v)))))
        )),
        (controllerName, Json.obj(
          ("Memory File Name", Json.fromString(memoryFileName)),
          ("Average Successes", Json.fromValues(avgSuccessFH.map(v => Json.fromDoubleOrNull(roundDouble2(v))))),
          ("Average Actions", Json.fromValues(avgActionsFH.map(v => Json.fromInt(v)))),
          ("Average Remaining Health", Json.fromValues(avgRemainingHealthFH.map(v => Json.fromDoubleOrNull(roundDouble2(v))))),
          ("Average remainingEnergy", Json.fromValues(avgRemainingEnergyFH.map(v => Json.fromDoubleOrNull(roundDouble2(v)))))
        ))
      )),
      ("Map Water", Json.obj(
        ("Random", Json.obj(
          ("Average Successes", Json.fromValues(rAvgSuccessMW.map(v => Json.fromDoubleOrNull(roundDouble2(v))))),
          ("Average Actions", Json.fromValues(rAvgActionsMW.map(v => Json.fromInt(v)))),
          ("Average Remaining Health", Json.fromValues(rAvgRemainingHealthMW.map(v => Json.fromDoubleOrNull(roundDouble2(v))))),
          ("Average remainingEnergy", Json.fromValues(rAvgRemainingEnergyMW.map(v => Json.fromDoubleOrNull(roundDouble2(v)))))
        )),
        ("Heuristic", Json.obj(
          ("Average Successes", Json.fromValues(hAvgSuccessMW.map(v => Json.fromDoubleOrNull(roundDouble2(v))))),
          ("Average Actions", Json.fromValues(hAvgActionsMW.map(v => Json.fromInt(v)))),
          ("Average Remaining Health", Json.fromValues(hAvgRemainingHealthMW.map(v => Json.fromDoubleOrNull(roundDouble2(v))))),
          ("Average remainingEnergy", Json.fromValues(hAvgRemainingEnergyMW.map(v => Json.fromDoubleOrNull(roundDouble2(v)))))
        )),
        (controllerName, Json.obj(
          ("Memory File Name", Json.fromString(memoryFileName)),
          ("Average Successes", Json.fromValues(avgSuccessMW.map(v => Json.fromDoubleOrNull(roundDouble2(v))))),
          ("Average Actions", Json.fromValues(avgActionsMW.map(v => Json.fromInt(v)))),
          ("Average Remaining Health", Json.fromValues(avgRemainingHealthMW.map(v => Json.fromDoubleOrNull(roundDouble2(v))))),
          ("Average remainingEnergy", Json.fromValues(avgRemainingEnergyMW.map(v => Json.fromDoubleOrNull(roundDouble2(v)))))
        ))
      ))
    )

    saveJsonFile(fileName, filePath, jsonData)

  }

}
