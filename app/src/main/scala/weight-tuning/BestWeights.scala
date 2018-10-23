package bestweights

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


object BestWeights {

  val handTuned: WeightsSet = new Individual(weights = List(
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
  )).generateWeightsSet

  val handTuned2: WeightsSet = new Individual(weights = List(
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
    2.0,
    1.0,
    1.0,
    1.0,
    0.5,
    0.4,
    10.0
  )).generateWeightsSet

  val lucky: WeightsSet = new Individual(weights = List(
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
  )).generateWeightsSet

  val king: WeightsSet = new Individual(weights = List(
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
  )).generateWeightsSet

  val firstRun: WeightsSet = new Individual(weights = List(
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
  )).generateWeightsSet

  val consistant: WeightsSet = new Individual(weights = List(
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
  )).generateWeightsSet


  val findHumanLongRun: WeightsSet = new Individual(weights = List(
    0.96,
    0.45,
    0.02,
    0.37,
    0.94,
    0.04,
    0.67,
    0.68,
    0.67,
    0.12,
    0.77,
    0.23,
    0.85,
    0.22,
    0.81,
    0.68,
    0.73,
    0.02,
    0.48,
    0.3,
    0.76,
    24.92
  )).generateWeightsSet


  val mapWaterLongRun: WeightsSet = new Individual(weights = List(
    0.35,
    0.05,
    0.4,
    0.83,
    0.98,
    0.69,
    0.37,
    0.96,
    0.37,
    0.79,
    0.86,
    0.35,
    1.0,
    0.04,
    0.52,
    0.55,
    0.25,
    0.25,
    0.4,
    0.35,
    0.54,
    15.48
  )).generateWeightsSet


}
