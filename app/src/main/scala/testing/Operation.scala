package operation

import io.circe._
import io.circe.parser._

import test._
import operation._
import scoutagent._
import environment._
import scoututil.Util._
import scoutagent.Event._
import scoutagent.State._
import jsonhandler.Encoder._
import filemanager.FileManager._
import scala.collection.mutable.{ArrayBuffer => AB}


class Operation(agent: Agent, environment: Environment, goal: Goal, maxActions: Option[Int]) {

  //------------------------ VARIABLES --------------------------------

  // EVENT LOG
  var eventLogShort: AB[LogItemShort] = AB()
  var eventLog: AB[LogItem] = AB()

  // IMPORTANT STUFF....
  val maxHealth: Double = agent.health
  val maxEnergyLevel: Double = agent.energyLevel
  val timeLimit: Option[Double] = goal.timeLimit

  // SHORT-TERM SCORE WEIGHTS
  val movementRewardWeight = 1.0
  val scanRewardWeight = 1.0
  val healthRewardWeight = 2.0
  val energyRewardWeight = 1.0
  val timeRewardWeight = if (timeLimit == None) 0.0 else 1.0
  val statusWeightsTotal = healthRewardWeight + energyRewardWeight + timeRewardWeight

  // LONG-TERM SCORE WEIGHTS
  val goalRewardWeight = 1.0
  val longTermHealthRewardWeight = 2.0
  val longTermEnergyRewardWeight = 1.0
  val longTermTimeRewardWeight = if (timeLimit == None) 0.0 else 1.0
  val longTermWeightsTotal = goalRewardWeight + longTermHealthRewardWeight + longTermEnergyRewardWeight + longTermTimeRewardWeight

  //--------------------------- FUNCTIONS --------------------------------------

  //------------------------------ RUN -----------------------------------------
  def run: Unit = {
    // Setup the agent
    // println("Agent Setting Up...")
    agent.setup
    // Have the agent explore until it completes its goal is inoperational
    // println("Agent run started...")
    maxActions match {
      case None => {
        while (agent.operational && !goal.isComplete) {
          val state = agent.getState()
          val action = agent.chooseAction()
          val event = agent.performAction(environment, action)
          // println(s"Action: $action")
          // Calculate Short-Term Score and Log
          val shortTermScore = scoreEventShortTerm(event)
          eventLogShort += new LogItemShort(state, action, event, shortTermScore)
          // Update Goal
          goal.update(environment, agent)
        }
      }
      case Some(ma) => {
        for (i <- 0 until ma ) if (agent.operational && !goal.isComplete) {
          val state = agent.getState()
          val action = agent.chooseAction()
          val event = agent.performAction(environment, action)
          // println(s"Action: $action")
          // Calculate Short-Term Score and Log
          val shortTermScore = scoreEventShortTerm(event)
          eventLogShort += new LogItemShort(state, action, event, shortTermScore)
          // Update Goal
          goal.update(environment, agent)
        }
      }
    }

    // Propagate Long-Term Score
    // println("Calculating Scores...")
    scoreEventsLongTerm()
    // Shut down agent
    // println("Shutting Agent Down...")
    agent.shutDown(getStateActionPairs())
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
  def scoreEventsLongTerm(): Unit = {
    val goalReward = (goal.percentComplete / 100.0) * goalRewardWeight
    val longTermHealthReward = (agent.health / maxHealth) * longTermHealthRewardWeight
    val longTermEnergyReward = (agent.energyLevel / maxEnergyLevel) * longTermEnergyRewardWeight
    val longTermTimeReward = timeLimit match {
      case None => 0.0
      case Some(tl) => Math.max(((tl - agent.clock) / tl), 0.0) * longTermTimeRewardWeight
    }
    val longTermScore = (goalReward + longTermHealthReward + longTermEnergyReward + longTermTimeReward) / longTermWeightsTotal
    for (i <- 0 until eventLogShort.size) {
      val item = eventLogShort(i)
      val scale = if (eventLogShort.size > 10) eventLogShort.size / 10 else 1.0
      val itemLongTermScore = longTermScore * Math.pow(0.9, i/scale)
      eventLog += new LogItem(item.state, item.action, item.event, item.shortTermScore, longTermScore)
    }
  }

  //------------------------ TEST METRIC DATA ----------------------------------
  def runData: RunData = new RunData(goal.percentComplete, eventLog.size, agent.health, agent.energyLevel)

  //------------------------ EXPORT EVENT LOG ----------------------------------
  def getStateActionPairs(): List[StateActionPair] = eventLog.map(_.getStateActionPair()).toList

  def printActions = for (item <- eventLog) println(item.action)

  def printEvents = {
    for (item <- eventLog) {
      println(s"${item.action}: ${item.event.msg}")
      println(s"       Short-Term Score: ${item.shortTermScore}")
      println(s"       Long-Term Score: ${item.longTermScore}")
    }
    println(agent.statusString())
    printOutcome
  }

  def printOutcome = {
    println()
    println(s"AGENT: ${agent.name}")
    println(s"Actions: ${eventLog.size}")
    println(s"Health: ${roundDouble2(agent.health)}")
    println(s"Energy: ${roundDouble2(agent.energyLevel)}")
    println(s"Goal Completion: ${roundDouble2(goal.percentComplete)}")
  }

  //------------------------- SAVE OPERATION -----------------------------------
  def saveOperation(fName: String) = {
    val jsonEnv = parse(encodeEnvironment(environment)) match {
      case Left(_) => Json.obj()
      case Right(jEnv) => jEnv
    }
    val jsonData = Json.obj(
      ("environment", jsonEnv),
      ("stateActionPairs", Json.fromValues(getStateActionPairs().map(_.roundOff(4).toJson())))
    )
    saveJsonFile(fName, operationRunPath, jsonData)
  }

}
