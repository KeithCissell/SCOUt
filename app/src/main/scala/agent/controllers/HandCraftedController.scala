package scoutagent.controller

import scoutagent._
import scoutagent.Event._
import scoutagent.State._
import scoututil.Util._

import scala.collection.mutable.{ArrayBuffer => AB}


class HandCraftedController() extends Controller {

  def setup: Unit = {}

  def selectAction(actions: List[String], state: AgentState): String = {
    actions(randomInt(0, actions.length - 1))
  }

  def shutDown(stateActionPairs: List[StateActionPair]): Unit = {}

}
