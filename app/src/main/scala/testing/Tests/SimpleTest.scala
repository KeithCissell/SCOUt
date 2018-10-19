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
import scala.collection.mutable.{Map => MutableMap}
import scala.collection.mutable.{ArrayBuffer => AB}


object SimpleTest {

  def main(args: Array[String]): Unit = {

    // Setup Test
    val simpleTest = new Test(
      testEnvironments = Map(),
      testTemplates = Map(
        "10by10NoMods" -> (10, 2),
        "BeginnerCourse" -> (25, 2)),
      controllers = Map(
        "Random" -> new RandomController(),
        "FindHuman" -> new FindHumanController(),
        "SCOUt" -> new SCOUtController("ElevationTest", "json", false, None)),
      sensors = List(
        new ElevationSensor(true),
        new DecibelSensor(false),
        new TemperatureSensor(false),
        new WaterSensor(false)),
      // goalTemplate = new FindAnomaliesTemplate(Map("Human" -> 1), None),
      goalTemplate = new MapElementsTemplate(List("Elevation"), None),
      maxActions = None,
      verbose = true)

    // Run Test
    simpleTest.run

  }

}
