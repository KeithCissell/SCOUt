package agent.controler

import agent._
import scoututil.Util._


class RandomControler() extends Controler {

  def selectAction(actions: List[String], state: String): String = {
    actions(randomInt(0, actions.length - 1))
  }

}
