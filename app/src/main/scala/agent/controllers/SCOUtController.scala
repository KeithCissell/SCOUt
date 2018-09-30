package scoutagent.controller

import io.circe._
import io.circe.parser._

import scoutagent._
import scoutagent.Event._
import scoutagent.State._
import filemanager.FileManager._
import jsonhandler.Decoder._
import scoututil.Util._

import scala.collection.mutable.{Map => MutableMap}
import scala.collection.mutable.{ArrayBuffer => AB}


class SCOUtController(
  val memoryFileName: String,
  val memoryExtention: String = "json",
  val training: Boolean
) extends Controller {

  // Holds a history of movements for reference
  val memory: AB[StateActionPair] = AB()

  def copy: Controller = new SCOUtController(memoryFileName, memoryExtention, training)

  def setup: Unit = {
    loadMemory()
  }

  def selectAction(actions: List[String], state: AgentState): String = {
    println(state.energyLevel)
    // Action Confidence (action -> similarity and outcome)
    val actionStateSimilarities: Map[String,SimilarityAverage] = actions.map(a => a -> calculateSimilarity(state, a)).toMap
    // Select action
    if (training) rouletteSelection(actionStateSimilarities)
    else eliteSelection(actionStateSimilarities)
  }

  def rouletteSelection(actionStateSimilarities: Map[String,SimilarityAverage]): String = {
    val sizeWeight = 0
    val similarityWeight = 1.0
    val shortTermScoreWeight = 1.0
    val longTermScoreWeight = 1.0
    val weightTotals = sizeWeight + similarityWeight + shortTermWeight + longTermWeight
    val offset = 0.5 // Ensures every action has a chance for selection
    val actionConfidence: Map[String,Double] = for ((a,s) <- actionStateSimilarities) yield {
      val sizeScore = s.numSimilarStates * sizeWeight
      val similarityScore = s.similarity * similarityWeight
      val stsScore = s.shortTermScore * shortTermWeight
      val ltsScore = s.longTermScore * longTermScoreWeight
      val scoreTotal = (sizeScore + similarityScore + stsScore + ltsScore) / weightTotals
      (a -> (scoreTotal + offset))
    }
    var selection = randomDouble(0.0, actionConfidence.values.foldLeft(0.0)(_ + _))
    for ((action, confidence) <- actionConfidence) {
      if (confidence >= selection) return action
      else selection -= confidence
    }
    return "null"
  }

  def eliteSelection(actionStateSimilarities: Map[String,SimilarityAverage]): String = {
    val sizeWeight = 0.0
    val similarityWeight = 1.0
    val shortTermScoreWeight = 1.0
    val longTermScoreWeight = 1.0
    val weightTotals = sizeWeight + similarityWeight + shortTermWeight + longTermWeight
    val actionConfidence: Map[String,Double] = for ((a,s) <- actionStateSimilarities) yield {
      val sizeScore = s.numSimilarStates * sizeWeight
      val similarityScore = s.similarity * similarityWeight
      val stsScore = s.shortTermScore * shortTermWeight
      val ltsScore = s.longTermScore * longTermScoreWeight
      val scoreTotal = (sizeScore + similarityScore + stsScore + ltsScore) / weightTotals
      (a -> scoreTotal)
    }
    var bestAction: Option[String] = None
    var bestConfidence: Option[Double] = None
    for ((action, confidence) <- actionConfidence) bestConfidence match {
      case None => {
        bestAction = Some(action)
        bestConfidence = Some(confidence)
      }
      case Some(bc) if (confidence > bc) => {
        bestAction = Some(action)
        bestConfidence = Some(confidence)
      }
      case _ => // no update
    }
    println(bestAction.getOrElse("null"))
    return bestAction.getOrElse("null")
  }

  def shutDown(stateActionPairs: List[StateActionPair]): Unit = {
    if (training) {
      val fps = 4 // floating points past decimal to store
      memory ++= stateActionPairs.map(_.roundOff(fps))
      saveMemory()
    }
  }

  // ---------------------------------MEMORY------------------------------------
  def loadMemory() = if (fileExists(memoryFileName, memoryFilePath, memoryExtention)) {
    val fileData = readFile(memoryFileName, memoryFilePath, memoryExtention)
    val loadedMemory = parse(fileData) match {
      case Left(_) => AB() // Memory not found or invalid
      case Right(jsonData) => extractStateActionMemoryIndexed(jsonData)
    }
    // println(s"Loaded ${loadedMemory.length} items")
    memory ++= loadedMemory
  }

  def saveMemory() = {
    val memoryJson = Json.fromValues(memory.map(_.toJsonIndexed()))
    // println(s"Memory length: ${memory.size}")
    // println("Saving file...")
    saveFile(memoryFileName, memoryFilePath, memoryExtention, memoryJson)
    // println("Save Complete")
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
  val movementSimilarityThreshhold = 0.5

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
    // println()
    // println(s"Action: $action")
    // println(s"Similar States: ${similarities.size}")
    // println(s"Average Similarity: $avgSimilarity")
    // println(s"Average STS: $avgSTS")
    // println(s"Average LTS: $avgLTS")
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
        val healthDiff = Math.abs(state.health - simState.health) * healthWeight
        val energyDiff = Math.abs(state.energyLevel - simState.energyLevel) * energyWeight
        val indicatorDiff = (if (es.indicator == ses.indicator) 0.0 else 1.0) * indicatorWeight
        val hazardDiff = (if (es.hazard == ses.hazard) 0.0 else 1.0) * hazardWeight
        val percentKnownDiff = Math.abs(es.percentKnownInRange - ses.percentKnownInRange) * percentKnownWeight
        (healthDiff + energyDiff + indicatorDiff + hazardDiff + percentKnownDiff) / weightTotals
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
    val healthDiff = Math.abs(state.health - simState.health) * healthWeight
    val energyDiff = Math.abs(state.energyLevel - simState.energyLevel) * energyWeight
    val forwardDiff = calculateQuadrantDiff(state, f, simState, sf) * forwardWeight
    val backDiff = calculateQuadrantDiff(state, b, simState, sb) * backWeight
    // Consider reflection of state
    val leftDiff = calculateQuadrantDiff(state, l, simState, sl) * leftWeight
    val rightDiff = calculateQuadrantDiff(state, r, simState, sr) * rightWeight
    val leftDiffReflect = calculateQuadrantDiff(state, l, simState, sr) * leftWeight
    val rightDiffReflect = calculateQuadrantDiff(state, r, simState, sl) * rightWeight
    // Calculate total direction differences
    val directionsDiff = if (leftDiff + rightDiff < leftDiffReflect + rightDiffReflect) {
      forwardDiff + backDiff + leftDiff + rightDiff
    } else {
      forwardDiff + backDiff + leftDiffReflect + rightDiffReflect
    }
    val overallDiff = (healthDiff + energyDiff + directionsDiff) / weightTotals
    return 1.0 - overallDiff
  }

  // Quadrant State Difference
  def calculateQuadrantDiff(state: AgentState, quadrant: String, simState: AgentState, simQuadrant: String): Double = {
    // Calculate differences
    val esDiffs = for (es <- state.elementStates) yield simState.getElementState(es.elementType) match {
      case None => 1.0
      case Some(ses) => calculateElementStateDiff(es, quadrant, ses, simQuadrant)
    }
    val overallDiff = if (esDiffs.length > 0) esDiffs.foldLeft(0.0)(_ + _) / esDiffs.length else 1.0
    return overallDiff
  }

  // Element State Difference
  def calculateElementStateDiff(es: ElementState, quadrant: String, ses: ElementState, simQuadrant: String): Double = {
    // Weights
    val indicatorWeight = 1.0
    val hazardWeight = 1.0
    val percentKnownWeight = 1.0
    val averageValueDifferentialWeight = 1.0
    val immediateValueDifferentialWeight = 1.0
    val weightTotals = indicatorWeight + hazardWeight + percentKnownWeight + averageValueDifferentialWeight + immediateValueDifferentialWeight
    // Important values
    val qs = es.getQuadrantState(quadrant)
    val sqs = ses.getQuadrantState(simQuadrant)
    // Calculate differences
    val indicatorDiff = (if (es.indicator == ses.indicator) 0.0 else 1.0) * indicatorWeight
    val hazardDiff = (if (es.hazard == ses.hazard) 0.0 else 1.0) * hazardWeight
    val percentKnownDiff = Math.abs(qs.percentKnown - sqs.percentKnown) * percentKnownWeight
    // Difference between Average vs. Current Value Differentials
    val averageValueDifferentialDiff = (qs.averageValueDifferential, sqs.averageValueDifferential) match {
      case (Some(avd), Some(savd)) => Math.abs(avd - savd) * averageValueDifferentialWeight
      case _ => 1.0
    }
    // Difference between Immediate vs. Current Value Differentials
    val immediateValueDifferentialDiff = (qs.immediateValueDifferential, sqs.immediateValueDifferential) match {
      case (Some(ivd), Some(sivd)) => Math.abs(ivd - sivd) * immediateValueDifferentialWeight
      case _ => 1.0
    }
    // return overall difference
    val overallDiff = (indicatorDiff + hazardDiff + percentKnownDiff + averageValueDifferentialDiff + immediateValueDifferentialDiff) / weightTotals
    return overallDiff
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
