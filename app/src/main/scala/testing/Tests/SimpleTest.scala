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
import scala.collection.mutable.{Map => MutableMap}
import scala.collection.mutable.{ArrayBuffer => AB}


object SimpleTest {

  def main(args: Array[String]): Unit = {

    // Setup Test
    val simpleTest = new Test(
      testEnvironments = Map(),
      testTemplates = Map(
        "EASY" -> (50, 1),
        "MEDIUM" -> (50, 1),
        "HARD" -> (50, 1)),
      controllers = Map(
        "Random" -> new RandomController(),
        "FindHuman" -> new FindHumanController(),
        "MapWater" -> new MapWaterController(),
      //   "SCOUt1" -> new SCOUtController("FHOfficialTemplatesTEST2", "json", false, BestWeights.findHumanLongRun),
      //   "SCOUt2" -> new SCOUtController("FHOfficialTemplatesTEST2", "json", false, BestWeights.hybridLongRun),
      //   "SCOUt3" -> new SCOUtController("HybridOfficialTEST", "json", false, BestWeights.hybridLongRun)),
      // sensors = List(
      //   new ElevationSensor(false),
      //   new DecibelSensor(true),
      //   new TemperatureSensor(true),
      //   new WaterSensor(false)),
      // goalTemplate = new FindAnomaliesTemplate(Map("Human" -> 1), None),
        "SCOUt1" -> new SCOUtController("MWOfficialTemplatesTEST2", "json", false, BestWeights.mapWaterLongRun),
        "SCOUt2" -> new SCOUtController("MWOfficialTemplatesTEST2", "json", false, BestWeights.hybridLongRun),
        "SCOUt3" -> new SCOUtController("HybridOfficialTEST", "json", false, BestWeights.hybridLongRun)),
      sensors = List(
        new ElevationSensor(false),
        new DecibelSensor(false),
        new TemperatureSensor(false),
        new WaterSensor(true)),
      goalTemplate = new MapElementsTemplate(List("Water Depth"), None),
      maxActions = None,
      verbose = true)

    // Run Test
    simpleTest.run

  }

}
