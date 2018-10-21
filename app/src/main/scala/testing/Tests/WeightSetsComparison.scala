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


object WeightSetsComparison {

  def main(args: Array[String]): Unit = {

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
        "SCOUt - Hand Tuned" -> new SCOUtController(memory, "json", false, BestWeights.handTuned),
        "SCOUt - Lucky" -> new SCOUtController(memory, "json", false, BestWeights.lucky),
        "SCOUt - The Chosen One" -> new SCOUtController(memory, "json", false, BestWeights.king),
        "SCOUt - GA Result 1" -> new SCOUtController(memory, "json", false, BestWeights.firstRun),
        "SCOUt - GA Result 2" -> new SCOUtController(memory, "json", false, BestWeights.consistant)),
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
