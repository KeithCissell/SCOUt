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


object FindHumanTesting {

  def main(args: Array[String]): Unit = {

    // Testing Setup
    val controllerName = "SCOUt"
    val memoryFileName = "FindHumanTEST5"
    val weightsSet = BestWeights.handTuned

    val agentSensors = List(
      new ElevationSensor(false),
      new DecibelSensor(true),
      new TemperatureSensor(true),
      new WaterSensor(false))

    val testEnvironments: Map[String,Int] = Map()
    val testTemplates = Map(
      "EASY" -> (25, 1),
      "MEDIUM" -> (15, 1),
      "HARD" -> (10, 1)
    )

    val goalTemplate = new FindAnomaliesTemplate(Map("Human" -> 1), None)

    val controllers = Map(
      "Random" -> new RandomController(),
      "Heuristic" -> new FindHumanController(),
      controllerName -> new SCOUtController(memoryFileName, "json", false, weightsSet))

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

  }

}
