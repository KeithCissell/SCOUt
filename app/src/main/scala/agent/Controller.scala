package scoutagent.controller

import scoutagent._
import scoutagent.State._

trait Controller {

  def copy: Controller

  def setup(mapHeight: Int, mapWidth: Int): Unit
  def selectAction(actions: List[String], state: AgentState): String
  def shutDown(stateActionPairs: List[StateActionPair]): Unit

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
