package environment.anomaly.seed

import scoututil.Util._
import environment.layer._
import environment.anomaly._

import scala.math._
import scala.util.Random


trait AnomalySeed {
  val anomalyName: String
  val formFields: String

  // def this(seedData: Map[String, String])

  def getAnomaly(): Anomaly
}

// Gives access to all the avialable anomaly types that can be seed generated
object AnomalySeedList {
  // List of all seeds set to default
  def defaultSeedList(): List[AnomalySeed] = List(
    new HumanSeed()
  )

  // Returns the form field for the requested element type
  def getSeedForm(anomalyType: String): String = anomalyType match {
    case "Human"        => HumanSeed().formFields
  }
}
