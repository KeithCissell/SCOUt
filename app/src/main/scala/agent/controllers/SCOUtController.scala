package agent.controller

import io.circe._
import io.circe.parser._

import agent._
import agent.Event._
import filemanager.FileManager._
import jsonhandler.Decoder._
import scoututil.Util._

import scala.collection.mutable.{Map => MutableMap}
import scala.collection.mutable.{ArrayBuffer => AB}


class SCOUtController(
  val memoryFileName: String,
  val training: Boolean
) extends Controller {

  // Holds a history of movements for reference
  val memory: AB[StateActionPair] = AB()

  def setup: Unit = {
    loadMemory()
  }

  def selectAction(actions: List[String], state: AgentState): String = {
    // Action Confidence (action -> confidence of successful outcome)
    val actionConfidence: Map[String,SimilarityAverage] = actions.map(a => a -> calculateSimilarity(state, a)).toMap

    actions(randomInt(0, actions.length - 1))
  }

  def shutDown(stateActionPairs: List[StateActionPair]): Unit = {
    if (training) {
      val fps = 4 // floating points past decimal to store
      memory ++= stateActionPairs.map(_.roundOff(fps))
      saveMemory()
    }
  }

  // ---------------------------------MEMORY------------------------------------

  def loadMemory() = if (fileExists(memoryFileName, memoryFilePath, "json")) {
    val fileData = readJsonFile(memoryFileName, memoryFilePath)
    val loadedMemory = parse(fileData) match {
      case Left(_) => AB() // Memory not found or invalid
      case Right(jsonData) => extractStateActionMemory(jsonData)
    }
    memory ++= loadedMemory
  }

  def saveMemory() = {
    val memoryJson = Json.fromValues(memory.map(_.toJson()))
    saveJsonFile(memoryFileName, memoryFilePath, memoryJson)
  }

  // ---------------------------------CONFIDENCE--------------------------------

  // Action Selection Confidence Equation
  val similarityWeight = 1.0
  val shortTermWeight = 1.0
  val longTermWeight = 1.0
  val weightTotals: Double = similarityWeight + shortTermWeight + longTermWeight
  def confidenceEQ(s: Double, sts: Double, lts: Double): Double = (s * similarityWeight + sts * shortTermWeight + lts * longTermWeight) / weightTotals

  // Variables
  val scanSimilarityThreshhold = 0.5
  val movementSimilarityThreshhold = 0.0

  // SIMILARITY
  // Find similar scan events and accumulate the strongest similarities
  // Calculate a confidence score based on the confidence equations
  def calculateSimilarity(state: AgentState, action: String): SimilarityAverage = {
    var similarities: AB[Similarity] = AB()
    val movementAction = isMovementAction(action)
    for (sap <- memory) if (movementAction == isMovementAction(sap.action)) movementAction match {
      case true => {
        val similarity = calculateSimilarityMovement(state, action, sap.state, sap.action)
        if (similarity >= movementSimilarityThreshhold) similarities += new Similarity(similarity, sap.shortTermScore, sap.longTermScore)
      }
      case false => {
        val similarity = calculateSimilarityScan(state, action, sap.state, sap.action)
        if (similarity >= scanSimilarityThreshhold) similarities += new Similarity(similarity, sap.shortTermScore, sap.longTermScore)
      }
    }
    val avgSimilarity: Double = if (similarities.size > 0) (similarities.map(_.similarity).fold(0.0)(_ + _) / similarities.size) else 0.0
    val avgSTS: Double = if (similarities.size > 0) (similarities.map(_.shortTermScore).fold(0.0)(_ + _) / similarities.size) else 0.0
    val avgLTS: Double = if (similarities.size > 0) (similarities.map(_.longTermScore).fold(0.0)(_ + _) / similarities.size) else 0.0

    println()
    println(s"Action: $action")
    println(s"Similar States: ${similarities.size}")
    println(s"Average Similarity: $avgSimilarity")
    println(s"Average STS: $avgSTS")
    println(s"Average LTS: $avgLTS")
    return new SimilarityAverage(similarities.size, avgSimilarity, avgSTS, avgLTS)
  }

  // SCAN SIMILARITY
  def calculateSimilarityScan(state: AgentState, elementType: String, simState: AgentState, simElementType: String): Double = {
    // Weights
    val healthWeight = 1.0
    val energyWeight = 1.0
    val indicatorWeight = 1.0
    val hazardWeight = 1.0
    val percentKnownWeight = 4.0
    val weightTotals = healthWeight + energyWeight + indicatorWeight + hazardWeight + percentKnownWeight
    // Calculate differences
    val elementState = state.getElementState(elementType)
    val simElementState = simState.getElementState(simElementType)
    val overallDiff = (elementState, simElementState) match {
      case (Some(es), Some(ses)) => {
        val healthDiff = Math.abs(state.health - simState.health)
        val energyDiff = Math.abs(state.energyLevel - simState.energyLevel)
        val indicatorDiff = if (es.indicator == ses.indicator) 0.0 else 1.0
        val hazardDiff = if (es.hazard == ses.hazard) 0.0 else 1.0
        val percentKnownDiff = Math.abs(es.percentKnownInRange - ses.percentKnownInRange)
        // println(s"${es.percentKnownInRange} - ${ses.percentKnownInRange}")
        (healthDiff * healthWeight + energyDiff * energyWeight + indicatorDiff * indicatorWeight + hazardDiff * hazardWeight + percentKnownDiff * percentKnownWeight) / weightTotals
      }
      case _ => 1.0
    }
    return 1.0 - overallDiff
  }

  // MOVEMENT SIMILARITY
  def calculateSimilarityMovement(state: AgentState, orientation: String, simState: AgentState, simOrientation: String): Double = {
    // Weights
    val healthWeight = 1.0
    val energyWeight = 1.0
    val forwardWeight = 2.0
    val backWeight = 1.0
    val leftWeight = 0.5
    val rightWeight = 0.5
    val weightTotals = healthWeight + energyWeight + forwardWeight + backWeight + leftWeight + rightWeight
    // Get state orientations
    val f = orientation
    val b = getOppositeOrientation(orientation)
    val l = getCounterClockwiseOrientation(orientation)
    val r = getClockwiseOrientation(orientation)
    // Get similar state orientations
    val sf = simOrientation
    val sb = getOppositeOrientation(simOrientation)
    val sl = getCounterClockwiseOrientation(simOrientation)
    val sr = getClockwiseOrientation(simOrientation)
    // Calculate differences
    val healthDiff = Math.abs(state.health - simState.health)
    val energyDiff = Math.abs(state.energyLevel - simState.energyLevel)
    val forwardDiff = calculateQuadrantDiff(state, f, simState, sf)
    val backDiff = calculateQuadrantDiff(state, b, simState, sb)
    // Consider relection of state
    val leftDiff = calculateQuadrantDiff(state, l, simState, sl)
    val rightDiff = calculateQuadrantDiff(state, r, simState, sr)
    val leftDiffReflect = calculateQuadrantDiff(state, l, simState, getOppositeOrientation(sl))
    val rightDiffReflect = calculateQuadrantDiff(state, r, simState, getOppositeOrientation(sr))
    // Calculate total direction differences
    val weightedDirectionsDiff = if (leftDiff + rightDiff < leftDiffReflect + rightDiffReflect) {
      forwardDiff * forwardWeight + backDiff * backWeight + leftDiff * leftWeight + rightDiff * rightWeight
    } else {
      forwardDiff * forwardWeight + backDiff * backWeight + leftDiffReflect * leftWeight + rightDiffReflect * rightWeight
    }
    val overallDiff = (healthDiff * healthWeight + energyDiff * energyWeight + weightedDirectionsDiff) / weightTotals
    return 1.0 - overallDiff
  }

  // Quadrant State Difference
  def calculateQuadrantDiff(state: AgentState, quadrant: String, simState: AgentState, simQuadrant: String): Double = {
    // Calculate differences
    val esDiffs = for (es <- state.elementStates) yield simState.getElementState(es.elementType) match {
      case None => 1.0
      case Some(ses) => calculateElementStateDiff(es, quadrant, ses, simQuadrant)
    }
    return esDiffs.foldLeft(0.0)(_ + _) / esDiffs.length
  }

  // Element State Difference
  def calculateElementStateDiff(es: ElementState, quadrant: String, ses: ElementState, simQuadrant: String): Double = {
    // Weights
    val indicatorWeight = 1.0
    val hazardWeight = 1.0
    val percentKnownWeight = 1.0
    val averageValueWeight = 1.0
    val immediateValueWeight = 1.0
    val weightTotals = indicatorWeight + hazardWeight + percentKnownWeight + averageValueWeight + immediateValueWeight
    // Important values
    val value = es.value
    val simValue = ses.value
    val qs = es.getQuadrantState(quadrant)
    val sqs = ses.getQuadrantState(simQuadrant)
    // Calculate differences
    val indicatorDiff = if (es.indicator == ses.indicator) 0.0 else 1.0
    val hazardDiff = if (es.hazard == ses.hazard) 0.0 else 1.0
    val percentKnownDiff = Math.abs(qs.percentKnown - sqs.percentKnown)
    // Difference between Average vs. Current Value Differentials
    val averageValueDiff = getDiffBetweenDifferentials(value, qs.averageValue, simValue, sqs.averageValue)
    // Difference between Immediate vs. Current Value Differentials
    val immediateValueDiff = getDiffBetweenDifferentials(value, qs.immediateValue, simValue, sqs.immediateValue)
    // return overall difference
    val overallDiff = (indicatorDiff * indicatorWeight + hazardDiff * hazardWeight + percentKnownDiff * percentKnownWeight + averageValueDiff * averageValueWeight + immediateValueDiff * immediateValueWeight) / weightTotals
    return overallDiff
  }

  def getDiffBetweenDifferentials(start1: Option[Double], end1: Option[Double], start2: Option[Double], end2: Option[Double]): Double = {
    val differential1 = (start1, end1) match {
      case (Some(sv), Some(ev)) => Some(sv - ev)
      case _ => None
    }
    val differential2 = (start2, end2) match {
      case (Some(sv), Some(ev)) => Some(sv - ev)
      case _ => None
    }
    val diff = (differential1, differential2) match {
      case (Some(d1), Some(d2)) => Math.abs(d1 - d2)
      case _ => 1.0
    }
    return diff
  }

  // Set of movement actions
  def isMovementAction(action: String): Boolean = Set("north","south","west","east").contains(action)
  def getOppositeOrientation(orientation: String): String = orientation match {
    case "north" => "south"
    case "south" => "north"
    case "west" => "east"
    case "east" => "west"
  }
  def getClockwiseOrientation(orientation: String): String = orientation match {
    case "north" => "east"
    case "south" => "west"
    case "west" => "north"
    case "east" => "south"
  }
  def getCounterClockwiseOrientation(orientation: String): String = orientation match {
    case "north" => "west"
    case "south" => "east"
    case "west" => "south"
    case "east" => "north"
  }

}

class Similarity(
  val similarity: Double,
  val shortTermScore: Double,
  val longTermScore: Double
) {}

class SimilarityAverage(
  val numSimilarStates: Int,
  val similarity: Double,
  val shortTermScore: Double,
  val longTermScore: Double
) {}
