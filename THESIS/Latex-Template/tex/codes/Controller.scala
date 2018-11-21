trait Controller {
  def setup(mapHeight: Int, mapWidth: Int): Unit
  def selectAction(actions: List[String], state: AgentState): String
  def shutDown(stateActionRewards: List[StateActionReward]): Unit
}
