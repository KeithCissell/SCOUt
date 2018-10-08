package scoutagent.controller

import io.circe._
import io.circe.parser._

import scoutagent._
import scoutagent.State._
import scoutagent.State.StateActionPair
import scoutagent.Event._
import filemanager.FileManager._
import jsonhandler.Decoder._
import scoututil.Util._

import scala.collection.mutable.{Map => MutableMap}
import scala.collection.mutable.{Set => MutableSet}
import scala.collection.mutable.{ArrayBuffer => AB}


class SCOUtController(
  val memoryFileName: String,
  val memoryExtention: String = "json",
  val training: Boolean,
) extends Controller {

  // MEMORY ATTRIBUTES
  val memory: AB[StateActionPair] = AB()
  var normalizedMemory: NormalizedStateActionPairs = new NormalizedStateActionPairs(Nil)
  val saveLateMemoryLimit: Int = 10
  val saveEarlyMemoryPercent: Double = 0.05
  val fps = 4 // floating points past decimal to store in memory values

  // TRAINING ATTRIBUTES
  val randomSelectThreshhold: Int = 5000

  // EXPLORATION INFLUENCE
  val actionHistory: MutableMap[(Int,Int),MutableSet[String]] = MutableMap()
  val repititionPenalty: Double = 0.5

  // DIFFERENCE COMPARISON WEIGHTS
  val stateDifferenceWeights = new StateDifferenceWeights(
    healthWeight = 0.0,
    energyWeight = 0.0,
    elementStateWeight = 1.0,
    totalQuadrantWeight = 1.0,
    elementDifferenceWeights = new ElementDifferenceWeights(
      indicatorWeight = 4.0,
      hazardWeight = 1.0,
      percentKnownInRangeWeight = 5.0,
      immediateKnownWeight = 2.0),
    quadrantDifferenceWeights = new QuadrantDifferenceWeights(
      indicatorWeight = 4.0,
      hazardWeight = 1.0,
      percentKnownWeight = 1.0,
      averageValueWeight = 2.0,
      immediateValueWeight = 3.0))

  // SELECTION WEIGHTS
  val trainingSelectionWeights = new SelectionWeights(
    movementSelectionWeights = new ActionSelectionWeights(
      predictedShortTermScoreWeight = 1.0,
      predictedLongTermScoreWeight = 1.5,
      confidenceWeight = 0.5),
    scanSelectionWeights = new ActionSelectionWeights(
      predictedShortTermScoreWeight = 1.0,
      predictedLongTermScoreWeight = 1.5,
      confidenceWeight = 0.5))

  val selectionWeights = new SelectionWeights(
    movementSelectionWeights = new ActionSelectionWeights(
      predictedShortTermScoreWeight = 2.0,
      predictedLongTermScoreWeight = 1.0,
      confidenceWeight = 1.0),
    scanSelectionWeights = new ActionSelectionWeights(
      predictedShortTermScoreWeight = 1.0,
      predictedLongTermScoreWeight = 1.0,
      confidenceWeight = 1.0))

  def copy: Controller = new SCOUtController(memoryFileName, memoryExtention, training)

  def setup(mapHeight: Int, mapWidth: Int): Unit = {
    loadMemory()
    normalizedMemory = new NormalizedStateActionPairs(memory.toList)
    for {
      x <- 0 until mapHeight
      y <- 0 until mapWidth
    } actionHistory += (x,y) -> MutableSet()
  }

  def shutDown(stateActionPairs: List[StateActionPair]): Unit = if (training) {
    val lateMemory: List[StateActionPair] = if (stateActionPairs.length > saveLateMemoryLimit) {
      stateActionPairs.slice(stateActionPairs.length - (saveLateMemoryLimit +1), stateActionPairs.length - 1)
    } else stateActionPairs
    val earlyMemory: List[StateActionPair] = if (stateActionPairs.length > saveLateMemoryLimit) {
      val sampleRate: Int = List(1, ((stateActionPairs.length - saveLateMemoryLimit) * saveEarlyMemoryPercent).toInt).max
      (for {
        i <- 0 to (stateActionPairs.length - saveLateMemoryLimit)
        if (i % sampleRate) == 0.0
      } yield stateActionPairs(i)).toList
    } else Nil
    memory ++= lateMemory.map(_.roundOff(fps)) ++= earlyMemory.map(_.roundOff(fps))
    saveMemory()
    // println()
    // println(s"Saving: ${sapsToSave.size}")
    // println()
  }

  // ---------------------------------MEMORY------------------------------------
  def loadMemory() = if (fileExists(memoryFileName, memoryFilePath, memoryExtention)) {
    val fileData = readFile(memoryFileName, memoryFilePath, memoryExtention)
    val loadedMemory = parse(fileData) match {
      case Left(_) => AB() // Memory not found or invalid
      case Right(jsonData) => extractStateActionMemoryIndexed(jsonData)
    }
    memory ++= loadedMemory
    // println()
    // println(s"Loaded ${loadedMemory.length} items")
    // println()
  }

  def saveMemory() = {
    val memoryJson = Json.fromValues(memory.map(_.toJsonIndexed()))
    saveFile(memoryFileName, memoryFilePath, memoryExtention, memoryJson)
    // println(s"Memory length: ${memory.size}")
  }

  // -----------------------------SELECTION-------------------------------------
  def selectAction(actions: List[String], state: AgentState): String = {
    val action =  if (training && memory.size < randomSelectThreshhold) randomSelect(actions)
                  else if (training) rouletteSelection(actions, state)
                  else eliteSelection(actions, state)
    // Add action to history
    actionHistory((state.xPosition, state.yPosition)) += action
    return action
  }

  // SELECTION METHODS
  def randomSelect(actions: List[String]): String = actions(randomInt(0, actions.length - 1))

  def rouletteSelection(actions: List[String], state: AgentState): String = {
    val predictedActionScores: List[ActionScorePrediction] = predictActionScores(actions, state)
    // Scores
    val equalChance = 0.1
    var scoreTotal = 0.0
    var actionScores: MutableMap[String,Double] = MutableMap()
    for (pas <- predictedActionScores) {
      var score =
        if (isMovementAction(pas.action)) pas.overallScore(trainingSelectionWeights.movementSelectionWeights)
        else pas.overallScore(trainingSelectionWeights.scanSelectionWeights)
      // reduce score of repetitive actions
      if (actionHistory((state.xPosition, state.yPosition)).contains(pas.action)) {
        if (isMovementAction(pas.action)) score *= repititionPenalty
        else score = 0.0
      }
      score += equalChance
      scoreTotal += score
      actionScores += (pas.action -> score)
      // pas.printPrediction
    }
    // Roulette Select
    var selectionValue = randomDouble(0.0, scoreTotal)
    for ((a, s) <- actionScores) {
      if (selectionValue <= s) return a
      else selectionValue -= s
    }
    return randomSelect(actions)
  }

  def eliteSelection(actions: List[String], state: AgentState): String = {
    val predictedActionScores: List[ActionScorePrediction] = predictActionScores(actions, state)
    // Best Action
    var bestAction = randomSelect(actions)
    var bestScore = 0.0
    for (pas <- predictedActionScores) {
      var score =
        if (isMovementAction(pas.action)) pas.overallScore(selectionWeights.movementSelectionWeights)
        else pas.overallScore(selectionWeights.scanSelectionWeights)
      // reduce score of repetitive actions
      if (actionHistory((state.xPosition, state.yPosition)).contains(pas.action)) {
        if (isMovementAction(pas.action)) score *= repititionPenalty
        else score = 0.0
      }
      if (score > bestScore) {
        bestAction = pas.action
        bestScore = score
      }
      // pas.printPrediction
    }
    return bestAction
  }

  // ---------------------SCORE PREDICTIONS--------------------------
  def predictActionScores(actions: List[String], state: AgentState): List[ActionScorePrediction] = {
    // Calculate Overall Differences
    val stateActionDifferences: List[StateActionDifference] = normalizedMemory.calculateStateActionDifferences(state, stateDifferenceWeights)
    // for (sad <- stateActionDifferences) sad.print

    // Confidence Related Variables
    val maxDifferenceCompared: Double = 0.4
    val minimumComparisons: Double = 10.0

    // Score Each Action
    val actionScorePredictions = for (action <- actions) yield{
      // Collect Scores of Similar States
      var shortTermScores: AB[Double] = AB()
      var longTermScores: AB[Double] = AB()
      var differences: AB[Double] = AB()
      // Check Through Memory
      for (sad <- stateActionDifferences) if (sad.action == action) {
        if (sad.overallDifference < maxDifferenceCompared) {
          shortTermScores += sad.shortTermScore
          longTermScores += sad.longTermScore
          differences += sad.overallDifference
        }
      }
      // Calculate Scores
      val predictedShortTermScore = if (shortTermScores.size > 0) shortTermScores.foldLeft(0.0)(_ + _) / shortTermScores.size else 0.5
      val predictedLongTermScore = if (longTermScores.size > 0) longTermScores.foldLeft(0.0)(_ + _) / longTermScores.size else 0.5
      val confidence =
        if (differences.size > minimumComparisons) (1.0 - (differences.foldLeft(0.0)(_ + _) / differences.size))
        else if (differences.size > 0) (1.0 - (differences.foldLeft(0.0)(_ + _) / differences.size)) * (differences.size.toDouble / minimumComparisons)
        else 0.0
      new ActionScorePrediction(action, predictedShortTermScore, predictedLongTermScore, confidence)
    }
    return actionScorePredictions
  }

}

class ActionScorePrediction(
  val action: String,
  val predictedShortTermScore: Double,
  val predictedLongTermScore: Double,
  val confidence: Double
) {
  def overallScore(weights: ActionSelectionWeights): Double = {
    val sts = predictedShortTermScore * weights.predictedShortTermScoreWeight
    val lts = predictedLongTermScore * weights.predictedLongTermScoreWeight
    val c = confidence * weights.confidenceWeight
    return (sts + lts + c) / weights.total
  }
  def printPrediction = {
    println()
    println(s"Action:         $action")
    println(s"Predicted STS:  $predictedShortTermScore")
    println(s"Predicted LTS:  $predictedLongTermScore")
    println(s"Confidence:     $confidence")
  }
}

class SelectionWeights(
  val movementSelectionWeights: ActionSelectionWeights,
  val scanSelectionWeights: ActionSelectionWeights
)

class ActionSelectionWeights(
  val predictedShortTermScoreWeight: Double,
  val predictedLongTermScoreWeight: Double,
  val confidenceWeight: Double
) {
  def total: Double = predictedShortTermScoreWeight + predictedLongTermScoreWeight + confidenceWeight
}
