package operation

import operation._
import agent._
import agent.Event._
import environment._
import scala.collection.mutable.{ArrayBuffer => AB}


class Operation(robot: Robot, environment: Environment, goal: Goal) {

  //------------------------ VARIABLES --------------------------------

  // EVENT LOG
  var eventLogShort: AB[LogItemShort] = AB()
  var eventLog: AB[LogItem] = AB()

  // IMPORTANT STUFF....
  val maxHealth: Double = robot.health
  val maxEnergyLevel: Double = robot.energyLevel
  val timeLimit: Option[Double] = goal.timeLimit

  // SHORT-TERM SCORE WEIGHTS
  val movementRewardWeight = 0.1
  val scanRewardWeight = 5.0
  val healthRewardWeight = 1.0
  val energyRewardWeight = 1.0
  val timeRewardWeight = if (timeLimit == None) 0.0 else 1.0
  val statusWeightsTotal = healthRewardWeight + energyRewardWeight + timeRewardWeight

  // LONG-TERM SCORE WEIGHTS

  //--------------------------- FUNCTIONS --------------------------------------

  //------------------------------ RUN -----------------------------------------
  def run: Unit = {
    // Have the robot explore until it completes its goal is inoperational
    println(robot.getState())
    println()
    while(robot.operational && !goal.isComplete){
      val state = robot.getState()
      val action = robot.chooseAction()
      val event = robot.performAction(environment, action)
      // Calculate Short-Term Score and Log
      val shortTermScore = scoreEventShortTerm(event)
      eventLogShort += new LogItemShort(state, action, event, shortTermScore)
      // Update Goal
      goal.update(environment, robot)
    }
    // Propagate Long-Term Score
    for (i <- 0 until eventLogShort.size) {
      val item = eventLogShort(i)
      val longTermScore = scoreEventLongTerm(item.event)
      eventLog += new LogItem(item.state, item.action, item.event, item.shortTermScore, longTermScore)
      println(s"${item.action}: ${item.event.msg}")
      println(s"       Short-Term Score: ${item.shortTermScore}")
      println(s"       Long-Term Score: $longTermScore")
    }
    println()
    println(robot.getState())
    println(s"GOAL COMPLETION: ${goal.percentComplete}")
  }

  //------------------------ SHORT-TERM SCORE --------------------------------
  def scoreEventShortTerm(event: Event): Double = event match {
    case e: Fatal => 0.0
    case e: Unsuccessful => statusRewards(e) / statusWeightsTotal
    case e: ScanSuccessful => (scanReward(e) + statusRewards(e)) / (scanRewardWeight + statusWeightsTotal)
    case e: MovementSuccessful => (movementReward(e) + statusRewards(e)) / (movementRewardWeight + statusWeightsTotal)
  }
  def scanReward(scanEvent: ScanSuccessful): Double = (scanEvent.newDiscoveries.toDouble / scanEvent.cellsScanned.toDouble) * scanRewardWeight
  def movementReward(movementEvent: MovementSuccessful): Double = (if (visitedBefore(movementEvent.x, movementEvent.y)) 0.0 else 1.0) * movementRewardWeight
  def statusRewards(event: Event): Double = healthReward(event) + energyReward(event) + timeReward(event)
  def healthReward(event: Event): Double = ((maxHealth - damageTaken(event)) / maxHealth) * healthRewardWeight
  def energyReward(event: Event): Double = ((maxEnergyLevel - energyUse(event)) / maxEnergyLevel) * energyRewardWeight
  def timeReward(event: Event): Double = timeLimit match {
    case None => 0.0
    case Some(tl) => ((tl - timeElapsed(event)) / tl) * timeRewardWeight
  }
  def visitedBefore(x: Int, y: Int): Boolean = {
    for (item <- eventLogShort) if (item.event.x == x && item.event.y == y) return true
    return false
  }
  def damageTaken(event: Event): Double = eventLogShort.lastOption match {
    case Some(item) => item.event.health - event.health
    case None => maxHealth - event.health
  }
  def energyUse(event: Event): Double = eventLogShort.lastOption match {
    case Some(item) => item.event.energyLevel - event.energyLevel
    case None => maxHealth - event.energyLevel
  }
  def timeElapsed(event: Event): Double = eventLogShort.lastOption match {
    case Some(item) => event.timeStamp - item.event.timeStamp
    case None => event.timeStamp
  }

  //------------------------ LONG-TERM SCORE ---------------------------------
  def scoreEventLongTerm(event: Event): Double = 0.0 // to-do

}