package scoutagent.controller

import scoutagent._
import scoutagent.Event._
import scoutagent.State._
import scoututil.Util._

import scala.collection.mutable.{ArrayBuffer => AB}
import scala.collection.mutable.{Map => MutableMap}


class FindHumanController() extends Controller {

  // Keeps track of visited cells
  var visitedCells: MutableMap[(Int,Int),Int] = MutableMap()

  def copy: Controller = new FindHumanController()

  def setup: Unit = {}

  def selectAction(actions: List[String], state: AgentState): String = {
    // Add Current Position to visited cells
    if (visitedCells.contains((state.xPosition, state.yPosition))) visitedCells((state.xPosition, state.yPosition)) += 1
    else visitedCells += (state.xPosition, state.yPosition) -> 1
    // Score possible movement action
    val movementScores: Map[String,Double] = (for {
      action <- actions
      if (isMovementAction(action))
    } yield action -> scoreMovementAction(action, state)).toMap
    val scanScores: Map[String,Double] = (for {
      action <- actions
      if (!isMovementAction(action))
    } yield action -> scoreScanAction(action, state)).toMap

    // Select Action
    val movementScoreThreshhold = 0.4
    val scanScoreThreshold = 0.4
    // First check for move above score threshhold
    var bestMove = "none"
    var bestMoveScore = 0.0
    for ((move,score) <- movementScores) if (score >= bestMoveScore){
      bestMove = move
      bestMoveScore = score
    }
    if (bestMoveScore >= movementScoreThreshhold) return bestMove
    // Next, check for scan score above threshhold
    var bestScan = "none"
    var bestScanScore = 0.0
    for ((elementType,score) <- scanScores) if (score >= bestScanScore) {
      bestScan = elementType
      bestMoveScore = score
    }
    if (bestScanScore >= scanScoreThreshold) return bestScan
    else return bestMove
  }

  def shutDown(stateActionPairs: List[StateActionPair]): Unit = {}

  // Scan action score
  def scoreScanAction(elementType: String, state: AgentState): Double = state.getElementState(elementType) match {
    case None => 0.0
    case Some(elementState) => {
      // Weights
      val indicatorWeight = 1.0
      val hazardWeight = 1.0
      val pkirWeight = 3.0
      val immediatesKnownWeight = 2.0
      val weightTotals = indicatorWeight + hazardWeight + pkirWeight + immediatesKnownWeight
      // Scores
      val iScore = (if (elementState.indicator) 1.0 else 0.0) * indicatorWeight
      val hScore = (if (elementState.hazard) 1.0 else 0.0) * hazardWeight
      val pkirScore = elementState.percentKnownInRange * pkirWeight
      val immediatesKnownScore = (elementState.immediateValuesKnown.toDouble / 4.0) * immediatesKnownWeight
      return iScore + hScore + pkirScore + immediatesKnownScore / weightTotals
    }
  }

  // Movement action score
  def scoreMovementAction(quadrant: String, state: AgentState): Double = {
    // Weights
    val elevationWeight = 1.0
    val decibelWeight = 1.0
    val temperatureWeight = 1.0
    val waterDepthWeight = 0.0
    val weightTotals = elevationWeight + decibelWeight + temperatureWeight + waterDepthWeight
    // Scores
    val elevationScore = state.getElementState("Elevation") match {
      case Some(es) => scoreElevation(quadrant, es) * elevationWeight
      case None => 0.5 * elevationWeight
    }
    val decibelScore = state.getElementState("Decibel") match {
      case Some(es) => scoreDecibel(quadrant, es) * decibelWeight
      case None => 0.5 * decibelWeight
    }
    val temperatureScore = state.getElementState("Temperature") match {
      case Some(es) => scoreTemperature(quadrant, es) * temperatureWeight
      case None => 0.5 * temperatureWeight
    }
    val waterDepthScore = state.getElementState("Water Depth") match {
      case Some(es) => scoreWaterDepth(quadrant, es) * waterDepthWeight
      case None => 0.5 * waterDepthWeight
    }
    // Visited cells get decreased score
    val score = elevationScore + decibelScore + temperatureScore + waterDepthScore / weightTotals
    if (visitedCells.contains((state.xPosition, state.yPosition))) return score / visitedCells((state.xPosition, state.yPosition)) + 1
    else return score
  }

  def scoreElevation(quadrant: String, elementState: ElementState): Double = {
    val qs = elementState.getQuadrantState(quadrant)
    // Weights
    val percentKnownWeight = 0.0
    val averageValueWeight = 0.0
    val immediateValueWeight = 2.0
    val weightTotals = percentKnownWeight + averageValueWeight + immediateValueWeight
    // Scores
    val pkScore = qs.percentKnown * percentKnownWeight
    val avScore = 0.0
    val imScore = qs.immediateValueDifferential match {
      case Some(v) if (v > 20) => 0.0
      case Some(v) if (v < 20) => 0.0
      case Some(_) => 1.0
      case None => 0.0
    }
    return pkScore + avScore + imScore / weightTotals
  }

  def scoreDecibel(quadrant: String, elementState: ElementState): Double = {
    val qs = elementState.getQuadrantState(quadrant)
    // Weights
    val percentKnownWeight = 1.0
    val averageValueWeight = 1.5
    val immediateValueWeight = 2.0
    val weightTotals = percentKnownWeight + averageValueWeight + immediateValueWeight
    // Scores
    val pkScore = qs.percentKnown * percentKnownWeight
    val avScore = qs.averageValueDifferential match {
      case Some(v) if (v >= 0.0) => 1.0
      case Some(_) => 0.0
      case None => 0.5
    }
    val imScore = qs.immediateValueDifferential match {
      case Some(v) if (v >= 0.0) => 1.0
      case Some(_) => 0.0
      case None => 0.5
    }
    return pkScore + avScore + imScore / weightTotals
  }

  def scoreTemperature(quadrant: String, elementState: ElementState): Double = {
    val qs = elementState.getQuadrantState(quadrant)
    // Weights
    val percentKnownWeight = 1.0
    val averageValueWeight = 1.5
    val immediateValueWeight = 2.0
    val weightTotals = percentKnownWeight + averageValueWeight + immediateValueWeight
    // Scores
    val pkScore = qs.percentKnown * percentKnownWeight
    val avScore = qs.averageValueDifferential match {
      case Some(v) if (v >= 0.0) => 1.0
      case Some(_) => 0.0
      case None => 0.5
    }
    val imScore = qs.immediateValueDifferential match {
      case Some(v) if (v >= 0.0) => 1.0
      case Some(_) => 0.0
      case None => 0.5
    }
    return pkScore + avScore + imScore / weightTotals
  }

  def scoreWaterDepth(quadrant: String, elementState: ElementState): Double = {
    val qs = elementState.getQuadrantState(quadrant)
    // Weights
    val percentKnownWeight = 0.0
    val averageValueWeight = 0.0
    val immediateValueWeight = 2.0
    val weightTotals = percentKnownWeight + averageValueWeight + immediateValueWeight
    // Scores
    val pkScore = qs.percentKnown * percentKnownWeight
    val avScore = qs.averageValueDifferential match {
      case Some(v) if (v > 0.0) => 0.25
      case Some(_) => 1.0
      case None => 0.0
    }
    val imScore = qs.immediateValueDifferential match {
      case Some(v) if (v > 0.0) => 0.0
      case Some(_) => 1.0
      case None => 0.0
    }
    return pkScore + avScore + imScore / weightTotals
  }

}
