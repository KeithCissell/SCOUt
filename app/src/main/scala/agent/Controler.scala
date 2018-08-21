package agent.controler

import agent._
import agent.Event._

import scala.collection.mutable.{ArrayBuffer => AB}


trait Controler {

  val maxHealth: Double = 100.0
  val maxEnergyLevel: Double = 100.0
  val timeLimit: Option[Double] = None
  var eventLog: AB[LogItem]

  def selectAction(actions: List[String], state: String): String

  def logEvent(state: String, action: String, event: Event): Unit = {
    val eventScore = scoreEvent(event)
    eventLog += new LogItem(state, action, event, eventScore)
    println(action + ": " + event.msg + "  =>  " + eventScore)
  }

  // Scores event on a scale of 0 to 1 (bad to good)
  val mainEventRewardWeight = 1.0
  val healthRewardWeight = 1.0
  val energyRewardWeight = 1.0
  val timeRewardWeight = if (timeLimit == None) 0.0 else 1.0
  val statusWeightsTotal = healthRewardWeight + energyRewardWeight + timeRewardWeight
  val weightsTotal = mainEventRewardWeight + statusWeightsTotal
  def scoreEvent(event: Event): Double = event match {
    case e: Fatal => 0.0
    case e: Unsuccessful => statusRewards(e) / statusWeightsTotal
    case e: ScanSuccessful => (scanReward(e) + statusRewards(e)) / weightsTotal
    case e: MovementSuccessful => (movementReward(e) + statusRewards(e)) / weightsTotal
  }
  def scanReward(scanEvent: ScanSuccessful): Double = (scanEvent.newDiscoveries / scanEvent.cellsScanned) * mainEventRewardWeight
  def movementReward(movementEvent: MovementSuccessful): Double = (if (visitedBefore(movementEvent.x, movementEvent.y)) 0.0 else 1.0) * mainEventRewardWeight
  def statusRewards(event: Event): Double = healthReward(event) + energyReward(event) + timeReward(event)
  def healthReward(event: Event): Double = ((maxHealth - damageTaken(event)) / maxHealth) * healthRewardWeight
  def energyReward(event: Event): Double = ((maxEnergyLevel - energyUse(event)) / maxEnergyLevel) * energyRewardWeight
  def timeReward(event: Event): Double = timeLimit match {
    case None => 0.0
    case Some(tl) => ((tl - timeElapsed(event)) / tl) * timeRewardWeight
  }

  def visitedBefore(x: Int, y: Int): Boolean = {
    for (logItem <- eventLog) if (logItem.event.x == x && logItem.event.y == y) return true
    return false
  }
  def damageTaken(event: Event): Double = eventLog.lastOption match {
    case Some(logItem) => logItem.event.health - event.health
    case None => maxHealth - event.health
  }
  def energyUse(event: Event): Double = eventLog.lastOption match {
    case Some(logItem) => logItem.event.energyLevel - event.energyLevel
    case None => maxHealth - event.energyLevel
  }
  def timeElapsed(event: Event): Double = eventLog.lastOption match {
    case Some(logItem) => event.timeStamp - logItem.event.timeStamp
    case None => event.timeStamp
  }
}
