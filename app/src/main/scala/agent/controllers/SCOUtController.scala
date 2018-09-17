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
    val actionConfidence: Map[String,Similarity] = actions.map(a => a -> calculateSimilarity(state, a)).toMap

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

  // Variables
  val maxSimilarities = 5
  val similarityWeight = 1.0
  val shortTermWeight = 1.0
  val longTermWeight = 1.0
  val weightTotals: Double = similarityWeight + shortTermWeight + longTermWeight
  def confidenceEQ(s: Double, sts: Double, lts: Double): Double = (s * similarityWeight + sts * shortTermWeight + lts * longTermWeight) / weightTotals

  // CONFIDENCE
  // Find similar scan events and accumulate the strongest similarities
  // Calculate a confidence score based on the confidence equations
  def calculateSimilarity(state: AgentState, action: String): Similarity = {
    var similarities: AB[Similarity] = AB()
    val movementAction = isMovementAction(action)
    for (sap <- memory) if (movementAction == isMovementAction(sap.action)) {
      val similarity = movementAction match {
        case true   => calculateSimilarityMovement(state, action, sap.state, sap.action)
        case false  => calculateSimilarityScan(state, action, sap.state, sap.action)
      }
      if (similarities.size < maxSimilarities) similarities += new Similarity(similarity, sap.shortTermScore, sap.longTermScore)
      else {
        var lowestSimilarity = similarity
        var lowestSimilarityIndx = -1
        for (i <- 0 until similarities.size) if (similarities(i).similarity < similarity) {
          lowestSimilarity = similarities(i).similarity
          lowestSimilarityIndx = i
        }
        if (lowestSimilarityIndx >= 0) similarities(lowestSimilarityIndx) = new Similarity(similarity, sap.shortTermScore, sap.longTermScore)
      }
    }
    val avgSimilarity: Double = if (similarities.size > 0) (similarities.map(_.similarity).fold(0.0)(_ + _) / similarities.size) else 0.0
    val avgSTS: Double = if (similarities.size > 0) (similarities.map(_.longTermScore).fold(0.0)(_ + _) / similarities.size) else 0.0
    val avgLTS: Double = if (similarities.size > 0) (similarities.map(_.shortTermScore).fold(0.0)(_ + _) / similarities.size) else 0.0

    println()
    println(s"Action: $action")
    println(s"Average Similarity: $avgSimilarity")
    println(s"Average STS: $avgSTS")
    println(s"Average LTS: $avgLTS")
    return new Similarity(avgSimilarity, avgSTS, avgLTS)
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
        val percentKnownDiff = Math.abs(es.percentKnown - ses.percentKnown)
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
    val esWeight = 3.0
    val weightTotals = healthWeight + energyWeight + esWeight
    // Calculate differences
    val healthDiff = Math.abs(state.health - simState.health)
    val energyDiff = Math.abs(state.energyLevel - simState.energyLevel)
    val esDiff = for (es <- state.elementStates) yield simState.getElementState(es.elementType) match {
      case None => 1.0
      case Some(ses) => {
        val diff = calculateElementStateDiff(es, orientation, ses, simOrientation, false)
        val reflectionDiff = calculateElementStateDiff(es, orientation, ses, simOrientation, true)
        Math.min(diff, reflectionDiff)
      }
    }
    val overallDiff = (healthDiff * healthWeight + energyDiff * energyWeight + esDiff.foldLeft(0.0)(_ + _) * esWeight) / weightTotals
    return 1.0 - overallDiff
  }

  // Element State Difference
  def calculateElementStateDiff(es: ElementState, orientation: String, ses: ElementState, simOrientation: String, reflect: Boolean): Double = {
    // Weights
    val indicatorWeight = 1.0
    val hazardWeight = 1.0
    val directionWeight = 1.0
    val weightTotals = indicatorWeight + hazardWeight + directionWeight * 4.0
    // Calculate differences
    // Get state orientations
    val f = orientation
    val b = getOppositeOrientation(orientation)
    val l = getCounterClockwiseOrientation(orientation)
    val r = getClockwiseOrientation(orientation)
    // Get similar state orientations
    val sf = simOrientation
    val sb = getOppositeOrientation(simOrientation)
    val sl = if (reflect) getClockwiseOrientation(simOrientation) else getCounterClockwiseOrientation(simOrientation)
    val sr = if (reflect) getCounterClockwiseOrientation(simOrientation) else getClockwiseOrientation(simOrientation)
    // Calculate diffs
    val indicatorDiff = if (es.indicator == ses.indicator) 0.0 else 1.0
    val hazardDiff = if (es.hazard == ses.hazard) 0.0 else 1.0
    val forwardDiff = calculateQuadrantDiff(es.getQuadrantState(f), es.value, ses.getQuadrantState(sf), ses.value)
    val backDiff = calculateQuadrantDiff(es.getQuadrantState(b), es.value, ses.getQuadrantState(sb), ses.value)
    val leftDiff = calculateQuadrantDiff(es.getQuadrantState(l), es.value, ses.getQuadrantState(sl), ses.value)
    val rightDiff = calculateQuadrantDiff(es.getQuadrantState(r), es.value, ses.getQuadrantState(sr), ses.value)
    val directionsDiff = forwardDiff * directionWeight + backDiff * directionWeight + leftDiff * directionWeight + rightDiff * directionWeight
    return (indicatorDiff * indicatorWeight + hazardDiff * hazardWeight + directionsDiff) / weightTotals
  }

  // Quadrant State Difference
  def calculateQuadrantDiff(qs: QuadrantState, value: Option[Double], sqs: QuadrantState, sValue: Option[Double]): Double = {
    // Weights
    val percentKnownWeight = 1.0
    val averageValueWeight = 1.0
    val immediateValueWeight = 1.0
    val weightTotals = percentKnownWeight + averageValueWeight + immediateValueWeight
    // Calculate differences
    val percentKnownDiff = Math.abs(qs.percentKnown - sqs.percentKnown)
    // Difference between Average vs. Current Value Differentials
    val currentAvgDifferential = (value, qs.averageValue) match {
      case (Some(cv), Some(av)) => Some(cv - av)
      case _ => None
    }
    val similarAvgDifferential = (sValue, sqs.averageValue) match {
      case (Some(cv), Some(av)) => Some(cv - av)
      case _ => None
    }
    val averageValueDiff = (currentAvgDifferential, similarAvgDifferential) match {
      case (Some(cad), Some(sad)) => Math.abs(cad - sad)
      case _ => 1.0
    }
    // Difference between Immediate vs. Current Value Differentials
    val currentImmDifferential = (sValue, qs.immediateValue) match {
      case (Some(cv), Some(iv)) => Some(cv - iv)
      case _ => None
    }
    val similarImmDifferential = (sValue, sqs.immediateValue) match {
      case (Some(cv), Some(iv)) => Some(cv - iv)
      case _ => None
    }
    val immediateValueDiff = (currentImmDifferential, similarImmDifferential) match {
      case (Some(cid), Some(sid)) => Math.abs(cid - sid)
      case _ => 1.0
    }
    // return overall difference
    val overallDiff = (percentKnownDiff * percentKnownWeight + averageValueDiff * averageValueWeight + immediateValueDiff * immediateValueWeight) / weightTotals
    return overallDiff
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
