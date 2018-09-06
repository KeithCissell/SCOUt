package agent.controller

import agent._

trait Controller {

  def selectAction(actions: List[String], state: AgentState): String

}
