package scoutagent.controller

import scoutagent._
import scoutagent.Event._
import scoutagent.State._
import scoututil.Util._

import scala.collection.mutable.{ArrayBuffer => AB}
import scala.collection.mutable.{Set => MutableSet}
import scala.collection.mutable.{Map => MutableMap}


class MapWaterController() extends Controller {

  // EXPLORATION INFLUENCE
  val actionHistory: MutableMap[(Int,Int),MutableSet[String]] = MutableMap()
  var repititionPenalty: Double = 0.5

  def copy: Controller = new MapWaterController()

  def setup(mapHeight: Int, mapWidth: Int): Unit = {
    for {
      x <- 0 until mapHeight
      y <- 0 until mapWidth
    } actionHistory += (x,y) -> MutableSet()
  }

  def selectAction(actions: List[String], state: AgentState): String = {
    // Score Possible Actions
    val actionScores: Map[String,Double] = (for (action <- actions) yield {
      // Predict Score
      val baseScore = if (isMovementAction(action)) scoreMovementAction(action, state)
                      else scoreScanAction(action, state)
      // Discourage Repitition
      if (actionHistory((state.xPosition, state.yPosition)).contains(action)) action -> (baseScore * repititionPenalty)
      else action -> baseScore
    }).toMap

    // Find the best Action
    var bestAction = "null"
    var bestScore = 0.0
    for ((action,score) <- actionScores) if (score >= bestScore) {
      bestAction = action
      bestScore = score
    }

    // for ((a,s) <- actionScores) {
    //   println()
    //   println(s"Action: $a")
    //   println(s"Score:  ${roundDouble2(s)}")
    // }

    // Record Selection and Return it
    actionHistory((state.xPosition, state.yPosition)) += bestAction
    return bestAction
  }

  def shutDown(stateActionPairs: List[StateActionPair]): Unit = {}

  // Scan action score
  def scoreScanAction(elementType: String, state: AgentState): Double = state.getElementState(elementType) match {
    case None => 0.0
    case Some(elementState) => {
      // Weights
      val indicatorWeight = 0.75
      val hazardWeight = 0.5
      val pkirWeight = 1.0
      val immediatesKnownWeight = 0.75
      val weightTotals = indicatorWeight + hazardWeight + pkirWeight + immediatesKnownWeight
      // Scores
      val iScore = (if (elementState.indicator) 1.0 else 0.0) * indicatorWeight
      val hScore = (if (elementState.hazard) 1.0 else 0.0) * hazardWeight
      val pkirScore = (1.0 - elementState.percentKnownInRange) * pkirWeight
      val immediatesKnownScore = ((4.0 - elementState.immediateValuesKnown.toDouble) / 4.0) * immediatesKnownWeight
      return (iScore + hScore + pkirScore + immediatesKnownScore) / weightTotals
    }
  }

  // Movement action score
  def scoreMovementAction(quadrant: String, state: AgentState): Double = {
    // Weights
    val elevationWeight = 1.0
    val decibelWeight = 0.0
    val temperatureWeight = 0.0
    val waterDepthWeight = 1.0
    val weightTotals = elevationWeight + decibelWeight + temperatureWeight + waterDepthWeight
    // Scores
    val elevationScore = state.getElementState("Elevation") match {
      case Some(es) => scoreElevation(quadrant, es) * elevationWeight
      case None => 0.5 * elevationWeight
    }
    val decibelScore = 0.0
    val temperatureScore = 0.0
    val waterDepthScore = state.getElementState("Water Depth") match {
      case Some(es) => scoreWaterDepth(quadrant, es) * waterDepthWeight
      case None => 0.5 * waterDepthWeight
    }
    // println()
    // println(s"Elevation:  ${roundDouble2(elevationScore)}")
    // println(s"Decibel:    ${roundDouble2(decibelScore)}")
    // println(s"Temp:       ${roundDouble2(temperatureScore)}")
    // println(s"Water:      ${roundDouble2(waterDepthScore)}")
    // Visited cells get decreased score
    return (elevationScore + decibelScore + temperatureScore + waterDepthScore) / weightTotals
  }

  def scoreElevation(quadrant: String, elementState: ElementState): Double = {
    val qs = elementState.getQuadrantState(quadrant)
    // Weights
    val percentKnownWeight = 0.5
    val averageValueWeight = 0.0
    val immediateValueWeight = 2.0
    val weightTotals = percentKnownWeight + averageValueWeight + immediateValueWeight
    // Scores
    val pkScore = (1.0 - qs.percentKnown) * percentKnownWeight
    val avScore = 0.0 * averageValueWeight
    val imScore = qs.immediateValueDifferential match {
      case Some(v) if (Math.abs(v) > 12.0) => 0.0
      case Some(_) => 1.0
      case None => 0.0
    }
    return (pkScore + avScore + imScore) / weightTotals
  }

  def scoreWaterDepth(quadrant: String, elementState: ElementState): Double = {
    val qs = elementState.getQuadrantState(quadrant)
    // Weights
    val percentKnownWeight = 1.0
    val averageValueWeight = 0.5
    val immediateValueWeight = 1.0
    val weightTotals = percentKnownWeight + averageValueWeight + immediateValueWeight
    // Scores
    val pkScore = (1.0 - qs.percentKnown) * percentKnownWeight
    val avScore = qs.averageValueDifferential match {
      case Some(v) if (v > 0.0) => 1.0
      case Some(_) => 0.25
      case None => 1.0
    }
    val imScore = qs.immediateValueDifferential match {
      case Some(v) if (v > 0.0) => 0.0
      case Some(_) => 1.0
      case None => 0.0
    }
    return (pkScore + avScore + imScore) / weightTotals
  }

}
