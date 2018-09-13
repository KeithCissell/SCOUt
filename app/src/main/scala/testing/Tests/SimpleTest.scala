package test

import io.circe._
import io.circe.parser._
import io.circe.syntax._

import agent._
import agent.controller._
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
        "10by10NoMods" -> (10, 10)),
      controllers = Map(
        "Random" -> new RandomController()),
      sensors = List(
        new ElevationSensor(false),
        new DecibelSensor(true),
        new TemperatureSensor(true),
        new WaterSensor(false)),
      goalTemplate = new FindAnomaliesTemplate(Map("Human" -> 1), None))

    // Run Test
    simpleTest.run

  }

}
