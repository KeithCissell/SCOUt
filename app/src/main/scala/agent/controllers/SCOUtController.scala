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
    val actionConfidence: Map[String,Double] = actions.map(a => a -> calculateConfidence(state, a)).toMap

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

  def loadMemory() = if (fileExists(memoryFilePath, memoryFileName, "json")) {
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
  val weightTotals: Double = similarityWeight * similarityWeight + shortTermWeight * shortTermWeight + longTermWeight * longTermWeight
  def confidenceEQ(s: Double, sts: Double, lts: Double): Double = (s + sts + lts) / weightTotals

  // CONFIDENCE
  // Find similar scan events and accumulate the strongest similarities
  // Calculate a confidence score based on the confidence equations
  def calculateConfidence(state: AgentState, action: String): Double = {
    var similarities: AB[Similarity] = AB()
    val movmentAction = isMovementAction(action)
    for (sap <- memory) if (movmentAction == isMovementAction(sap.action)) {
      val similarity = movmentAction match {
        case true   => calculateSimilarityMovement(state, action, sap.state, sap.action)
        case false  => calculateSimilarityScan(state, action, sap.state, sap.action)
      }
      if (similarities.size < maxSimilarities) similarities += new Similarity(similarity, sap.shortTermScore, sap.longTermScore)
      else for (i <- 0 until similarities.size) {
        if (similarities(i).similarity < similarity) similarities(i) = new Similarity(similarity, sap.shortTermScore, sap.longTermScore)
      }
    }
    val confidences = similarities.map(s => confidenceEQ(s.similarity, s.shortTermScore, s.longTermScore))
    return confidences.fold(0.0)(_ + _) / confidences.length.toDouble
  }

  // SCAN SIMILARITY
  def calculateSimilarityScan(state: AgentState, elementType: String, simState: AgentState, simElementType: String): Double = {
    val elementState = state.getElementState(elementType)
    val simElementState = simState.getElementState(simElementType)
    val difference = (elementState, simElementState) match {
      case (Some(es), Some(ses)) => {
        val healthDiff = Math.abs(state.health - simState.health)
        val energyDiff = Math.abs(state.energyLevel - simState.energyLevel)
        val indicatorDiff = if (es.indicator == ses.indicator) 0.0 else 1.0
        val hazardDiff = if (es.hazard == ses.hazard) 0.0 else 1.0
        val percentKnownDiff = Math.abs(es.percentKnown - ses.percentKnown)
        (healthDiff + energyDiff + indicatorDiff + hazardDiff + percentKnownDiff) / 5.0
      }
      case _ => 1.0
    }
    return 1.0 - difference
  }

  // MOVEMENT SIMILARITY
  def calculateSimilarityMovement(state: AgentState, orientation: String, simState: AgentState, simOrientation: String): Double = {
    val healthDiff = Math.abs(state.health - simState.health)
    val energyDiff = Math.abs(state.energyLevel - simState.energyLevel)
    val esDiff = for (es <- state.elementStates) yield simState.getElementState(es.elementType) match {
      case None => 1.0
      case Some(ses) => {
        val similarity = calculateElementStateDiff(es, orientation, ses, simOrientation, false)
        val reflectionSimilarity = calculateElementStateDiff(es, orientation, ses, simOrientation, true)
        Math.max(similarity, reflectionSimilarity)
      }
    }
    return 1.0 - (healthDiff + energyDiff + esDiff.foldLeft(0.0)(_ + _)) / (2 + state.elementStates.length)
  }

  // Element State Difference
  def calculateElementStateDiff(es: ElementState, orientation: String, ses: ElementState, simOrientation: String, reflect: Boolean): Double = {
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
    val valueDiff = (es.value, ses.value) match {
      case (Some(v), Some(sv)) => Math.abs(v - sv)
      case _ => 1.0
    }
    val forwardDiff = calculateQuadrantDiff(es.getQuadrantState(f), es.getQuadrantState(sf))
    val backDiff = calculateQuadrantDiff(es.getQuadrantState(b), es.getQuadrantState(sb))
    val leftDiff = calculateQuadrantDiff(es.getQuadrantState(l), es.getQuadrantState(sl))
    val rightDiff = calculateQuadrantDiff(es.getQuadrantState(r), es.getQuadrantState(sr))
    return (indicatorDiff + hazardDiff + forwardDiff + backDiff + leftDiff + rightDiff) / 6.0
  }

  // Quadrant State Difference
  def calculateQuadrantDiff(qs: QuadrantState, sqs: QuadrantState): Double = {
    val percentKnownDiff = Math.abs(qs.percentKnown - sqs.percentKnown)
    val averageValueDiff = (qs.averageValue, sqs.averageValue) match {
      case (Some(av), Some(sav)) => Math.abs(av - sav)
      case _ => 1.0
    }
    val immediateValueDiff = (qs.immediateValue, sqs.immediateValue) match {
      case (Some(iv), Some(siv)) => Math.abs(iv - siv)
      case _ => 1.0
    }
    return (percentKnownDiff + averageValueDiff + immediateValueDiff) / 3.0
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
