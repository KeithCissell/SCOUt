package weighttuning

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
import org.joda.time.DateTime
import scala.collection.mutable.{Set => MutableSet}
import scala.collection.mutable.{Map => MutableMap}
import scala.collection.mutable.{ArrayBuffer => AB}


object WeightTuning {
  // GA Attributes
  val populationSize: Int = 10
  val numGenerations: Int = 50
  val elites: Int = 2
  val maxHealth: Double = 100.0
  val maxEnergy: Double = 100.0
  val maxActions: Int = 75
  val maxCompared: Double = 30.0 // for special weight "minimumComparisons"
  def mutationRate(currentGeneration: Int): Double = List(((numGenerations.toDouble * 1.2) - currentGeneration.toDouble), numGenerations.toDouble).min / numGenerations.toDouble

  // Weights Object
  class WeightsSet(
    val stateDifferenceWeights: StateDifferenceWeights,
    val selectionWeights: SelectionWeights,
    val repititionPenalty: Double,
    val maxDifferenceCompared: Double,
    val minimumComparisons: Int
  )

  // Individual
  class Individual(
    val weights: List[Double], // 22 weights
    var avgActions: Option[Int] = None,
    var avgRemainingHealth: Option[Double] = None,
    var avgRemainingEnergy: Option[Double] = None,
    var avgGoalCompletion: Option[Double] = None
  ) {
    def generateWeightsSet: WeightsSet = new WeightsSet(
      stateDifferenceWeights = new StateDifferenceWeights(
        healthWeight = weights(0),
        energyWeight = weights(1),
        elementStateWeight = weights(2),
        totalQuadrantWeight = weights(3),
        elementDifferenceWeights = new ElementDifferenceWeights(
          indicatorWeight = weights(4),
          hazardWeight = weights(5),
          percentKnownInRangeWeight = weights(6),
          immediateKnownWeight = weights(7)),
        quadrantDifferenceWeights = new QuadrantDifferenceWeights(
          indicatorWeight = weights(8),
          hazardWeight = weights(9),
          percentKnownWeight = weights(10),
          averageValueWeight = weights(11),
          immediateValueWeight = weights(12))),
      selectionWeights = new SelectionWeights(
        movementSelectionWeights = new ActionSelectionWeights(
          predictedShortTermScoreWeight = weights(13),
          predictedLongTermScoreWeight = weights(14),
          confidenceWeight = weights(15)),
        scanSelectionWeights = new ActionSelectionWeights(
          predictedShortTermScoreWeight = weights(16),
          predictedLongTermScoreWeight = weights(17),
          confidenceWeight = weights(18))),
      repititionPenalty = weights(19),
      maxDifferenceCompared = weights(20),
      minimumComparisons = weights(21).toInt
    )

    def fitness: Double = {
      val aa = ((maxActions - avgActions.getOrElse(maxActions).toDouble) / maxActions.toDouble) * 1.0
      val arh = (avgRemainingHealth.getOrElse(0.0) / maxHealth) * 1.0
      val are = (avgRemainingEnergy.getOrElse(0.0) / maxEnergy) * 1.0
      val agc = (avgGoalCompletion.getOrElse(0.0) / 100.0) * 7.0
      return (aa + arh + are + agc) / 10.0
    }

    def print = {
      println()
      println(s"Avg Actions:          ${avgActions.getOrElse(0)}")
      println(s"Avg Remaining Health: ${roundDouble2(avgRemainingHealth.getOrElse(0.0))}")
      println(s"Avg Remaining Energy: ${roundDouble2(avgRemainingEnergy.getOrElse(0.0))}")
      println(s"Avg Goal Completion:        ${roundDouble2(avgGoalCompletion.getOrElse(0.0))}")
      println(s"FITNESS: ${fitness}")
    }
  }

  // Crossover Two Individuals
  def crossover(ind1: Individual, ind2: Individual): Individual = {
    val cut1: Int = randomInt(0, ind1.weights.length - 1)
    val cut2: Int = randomInt(cut1 + 1, ind1.weights.length - 1)
    // Create new individual
    var weights: AB[Double] = AB()
    for (i <- 0 until ind1.weights.length) i match {
      case i if i < cut1 => weights += ind1.weights(i)
      case i if i < cut2 => weights += ind2.weights(i)
      case _ => weights += ind1.weights(i)
    }
    return new Individual(weights = weights.toList)
  }

  // Mutate Individual
  def mutate(ind: Individual, mutationRate: Double): Individual = {
    val chanceToMutate = 0.4
    val maxMutation = 0.5 * mutationRate
    val maxMutation2 = 0.5 * maxCompared * mutationRate
    // Mutate
    var weights: AB[Double] = AB()
    for (i <- 0 until ind.weights.length) if (randomDouble(0.0, 1.0) < chanceToMutate) {
      if (i != 21) {
        val change = randomDouble(-maxMutation, maxMutation)
        weights += List(List(0.0, ind.weights(i) + change).max, 1.0).min
      } else {
        val change = randomDouble(-maxMutation2, maxMutation2)
        weights += List(List(0.0, ind.weights(21) + change).max, maxCompared).min
      }
    } else weights += ind.weights(i)
    return new Individual(weights = weights.toList)
  }

  // Roullette Select Individual
  def rouletteSelect(inds: List[Individual]): Individual = {
    var scoreTotal = 0.0
    for (ind <- inds) scoreTotal += ind.fitness
    var selection = randomDouble(0.0, scoreTotal)
    var selectedInd: Individual = inds(0)
    for (ind <- inds) {
      if (ind.fitness >= selection) selectedInd = ind
      else selection -= ind.fitness
    }
    return selectedInd
  }

  // Save Population Data to File
  def savePopulation(runName: String, fileTag: String, population: List[Individual]): Unit = {
    val jsonInds: List[Json] = for (ind <- population) yield Json.obj(
      ("weights", Json.fromValues(ind.weights.map(w => Json.fromDoubleOrNull(roundDouble2(w))))),
      ("avgActions", Json.fromInt(ind.avgActions.getOrElse(0))),
      ("avgRemainingHealth", Json.fromDoubleOrNull(roundDouble2(ind.avgRemainingHealth.getOrElse(0.0)))),
      ("avgRemainingEnergy", Json.fromDoubleOrNull(roundDouble2(ind.avgRemainingEnergy.getOrElse(0.0)))),
      ("avgGoalCompletion", Json.fromDoubleOrNull(roundDouble2(ind.avgGoalCompletion.getOrElse(0.0)))),
      ("fitness", Json.fromDoubleOrNull(roundDouble2(ind.fitness)))
    )
    val jsonData = Json.fromValues(jsonInds)
    val fileName = runName + "-" + fileTag + "-" + new DateTime().toString("yyyy-MM-dd-HH-mm")
    saveJsonFile(fileName, gaOutputPath, jsonData)
  }

  // Save Average Generation Fitness's
  def saveGenerations(runName: String, af: List[Double], afp: List[Double]): Unit = {
    val jsonData = Json.obj(
      ("averageFitnessTotalPopulation", Json.fromValues(af.map(a => Json.fromDoubleOrNull(a)))),
      ("averageFitnessNewPopulation", Json.fromValues(afp.map(a => Json.fromDoubleOrNull(a))))
    )
    val fileName = runName + "-Averages-" + new DateTime().toString("yyyy-MM-dd-HH-mm")
    saveJsonFile(fileName, gaOutputPath, jsonData)
  }

}
