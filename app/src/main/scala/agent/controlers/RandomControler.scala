package agent.controler

import agent._
import agent.Event._
import scoututil.Util._

import scala.collection.mutable.{ArrayBuffer => AB}


class RandomControler(
      maxHealth: Double = 100.0,
      maxEnergyLevel: Double = 100.0,
      timeLimit: Option[Double] = None) extends Controler {

  var eventLog: AB[LogItem] = AB()

  def selectAction(actions: List[String], state: AgentState): String = {
    actions(randomInt(0, actions.length - 1))
  }

}
