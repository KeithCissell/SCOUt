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


object WeightSetsComparison {

  def main(args: Array[String]): Unit = {

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

    val pureLuck = new Individual(weights = List(
      0.47,
      0.26,
      0.03,
      0.66,
      0.78,
      0.31,
      0.9,
      0.46,
      0.51,
      0.12,
      0.4,
      0.61,
      0.54,
      0.19,
      0.7,
      0.39,
      0.08,
      0.87,
      0.9,
      0.75,
      0.25,
      20.54
    ))

    val giftFromGod = new Individual(weights = List(
      0.28,
      0.82,
      0.67,
      0.4,
      0.43,
      0.47,
      0.23,
      0.75,
      0.49,
      0.18,
      0.2,
      0.46,
      0.46,
      0.28,
      0.52,
      0.4,
      0.88,
      0.62,
      0.01,
      0.55,
      0.46,
      3.14
    ))

    val gaResult1 = new Individual(weights = List(
      0.71,
      0.88,
      0.9,
      0.58,
      0.54,
      0.53,
      0.87,
      0.08,
      0.78,
      0.55,
      0.27,
      0.46,
      0.17,
      0.51,
      0.01,
      0.95,
      0.2,
      0.94,
      0.28,
      0.46,
      0.66,
      13.26
    ))

    val gaResult2 = new Individual(weights = List(
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

    val memory = "LongTrainingTest2000"

    // Setup Test
    val simpleTest = new Test(
      testEnvironments = Map(),
      testTemplates = Map(
        "10by10NoMods" -> (10, 2),
        "BeginnerCourse" -> (15, 2)),
      controllers = Map(
        "Random" -> new RandomController(),
        "FindHuman" -> new FindHumanController(),
        "SCOUt - Hand Tuned" -> new SCOUtController(memory, "json", false, Some(handTuned.generateWeightsSet)),
        "SCOUt - Lucky" -> new SCOUtController(memory, "json", false, Some(pureLuck.generateWeightsSet)),
        "SCOUt - The Chosen One" -> new SCOUtController(memory, "json", false, Some(giftFromGod.generateWeightsSet)),
        "SCOUt - GA Result 1" -> new SCOUtController(memory, "json", false, Some(gaResult1.generateWeightsSet)),
        "SCOUt - GA Result 2" -> new SCOUtController(memory, "json", false, Some(gaResult2.generateWeightsSet))),
      sensors = List(
        new ElevationSensor(false),
        new DecibelSensor(true),
        new TemperatureSensor(true),
        new WaterSensor(false)),
      goalTemplate = new FindAnomaliesTemplate(Map("Human" -> 1), None),
      maxActions = None)

    // Run Test
    simpleTest.run

  }

}
