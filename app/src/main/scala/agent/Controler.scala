package agent.controler

import agent._

trait Controler {

  def selectAction(actions: List[String], state: AgentState): String

}
