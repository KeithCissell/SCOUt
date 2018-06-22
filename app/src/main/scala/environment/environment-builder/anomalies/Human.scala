package environment.anomaly

import environment.anomaly._
import environment.effect._
import environment.element._


class Human(
  val name: String = "Human",
  val area: Double = 6.0,
  val effects: List[Effect] = List(
    new Sound(seed = new Decibel(40.0)),
    new Heat(seed = new Temperature(98.6))
  )
) extends Anomaly {}


package seed {
  case class HumanSeed(
    val anomalyName: String = "Human",
    val formFields: String = """{
      "field-keys": [
      ],
      "fields": {
      }
    }"""
  ) extends AnomalySeed {
    def this(seedData: Map[String, String]) {
      this()
    }
    def getAnomaly(): Anomaly = new Human()
  }
}
