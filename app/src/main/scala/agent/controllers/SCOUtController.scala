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

  // Set of movement actions
  val movementActions: Set[String] = Set("north","south","west","east")

  def setup: Unit = {
    loadMemory()
  }

  def selectAction(actions: List[String], state: AgentState): String = {
    // Action Confidence (action -> confidence of successful outcome)
    val actionConfidence: Map[String,Double] = actions.map(a => a -> calculateConfidence(state, a)).toMap

    actions(randomInt(0, actions.length - 1))
  }

  def shutDown(stateActionPairs: List[StateActionPair]): Unit = {
    memory ++= stateActionPairs
    saveMemory()
  }

  // ---------------------------------MEMORY------------------------------------

  def loadMemory() = {
    println("loading memory...")
    val fileData = readJsonFile(memoryFileName, memoryFilePath)
    val loadedMemory = parse(fileData) match {
      case Left(_) => AB() // Memory not found or invalid
      case Right(jsonData) => extractStateActionMemory(jsonData)
    }
    memory ++= loadedMemory
  }

  def saveMemory() = {
    println(s"Memory Items: ${memory.size}")
    println("saving memory...")
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
    val isMovmentAction = movementActions.contains(action)
    for (sap <- memory) if (isMovmentAction == movementActions.contains(sap.action)) {
      val similarity = isMovmentAction match {
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

  // MOVEMENT SIMILARITY
  def calculateSimilarityMovement(state: AgentState, orientation: String, simState: AgentState, simOrientation: String): Double = 0.0

  // SCAN SIMILARITY
  def calculateSimilarityScan(state: AgentState, elementType: String, simState: AgentState, simElementType: String): Double = {
    val elementState = state.getElementState(elementType)
    val simElementState = simState.getElementState(simElementType)
    val similarity = (elementState, simElementState) match {
      case (None, _) => 0.0
      case (_, None) => 0.0
      case (Some(es), Some(ses)) => {
        val healthDiff = Math.abs(state.health - simState.health)
        val energyDiff = Math.abs(state.energyLevel - simState.energyLevel)
        val indicatorDiff = if (es.indicator == ses.indicator) 1.0 else 0.0
        val hazardDiff = if (es.hazard == ses.hazard) 1.0 else 0.0
        val percentKnownDiff = Math.abs(es.percentKnown - ses.percentKnown)
        (healthDiff + energyDiff + indicatorDiff + hazardDiff + percentKnownDiff) / 5.0
      }
    }
    return similarity
  }

}

class Similarity(
  val similarity: Double,
  val shortTermScore: Double,
  val longTermScore: Double
) {}
