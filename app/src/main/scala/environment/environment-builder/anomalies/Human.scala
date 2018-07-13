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
) extends Anomaly {
  def this(formData: Map[String, String]) = this(
    area = formData("Area").toDouble,
    effects = List(
      new Sound(seed = new Decibel(formData("Sound").toDouble)),
      new Heat(seed = new Temperature(formData("Heat").toDouble))
    )
  )
}

object HumanForm {
  def formFields(): String = """{
    "field-keys": [
      "Area",
      "Sound",
      "Heat"
    ],
    "fields": {
      "Area": {
        "type": "number",
        "unit": "ft (diameter)",
        "value": 6,
        "lowerBound": 2,
        "upperBound": 10
      },
      "Sound": {
        "type": "number",
        "unit": "dB",
        "value": 40.0,
        "lowerBound": 0,
        "upperBound": 100
      },
      "Heat": {
        "type": "number",
        "unit": "°F",
        "value": 98.6,
        "lowerBound": 95,
        "upperBound": 110
      }
    }
  }"""
}
