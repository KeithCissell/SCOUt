package scoutagent.controller

import scoutagent._
import scoutagent.State._

trait Controller {

  def setup: Unit
  def selectAction(actions: List[String], state: AgentState): String
  def shutDown(stateActionPairs: List[StateActionPair]): Unit

}
