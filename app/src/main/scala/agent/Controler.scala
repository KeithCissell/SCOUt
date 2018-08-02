package agent.controler

trait Controler {
  def selectAction(actions: List[String], state: String): String
}
