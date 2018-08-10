package agent.controler

import agent._
import agent.Event._

import scala.collection.mutable.{ArrayBuffer => AB}


trait Controler {

  var eventLog: AB[LogItem]

  def selectAction(actions: List[String], state: String): String

  def logEvent(state: String, action: String, event: Event): Unit = {
    val eventScore = scoreEvent(event)
    eventLog += new LogItem(state, action, event, eventScore)
    println(action + ": " + event.msg)
  }

  def scoreEvent(event: Event): Double = event match {
    case _ => 0.0 // to-do
  }

}
