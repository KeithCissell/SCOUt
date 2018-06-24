package environment.element

import scoututil.Util._
import environment.layer._
import environment.element._
import environment.element.seed._

import scala.collection.mutable.{ArrayBuffer => AB}


class Temperature(var value: Option[Double]) extends Element {
  val name = "Temperature"
  val unit = "°F"
  val constant = false
  val radial = false
  val lowerBound = -200.0
  val upperBound = 200.0
  def this(d: Double) = this(Some(d))
  def this()          = this(None)
}

package seed {
  case class TemperatureSeed(
    val elementName: String = "Temperature",
    val dynamic: Boolean = true,
    val average: Double = 70.0,
    val deviation: Double = 0.2,
    val formFields: String = """{
      "field-keys": [
        "Average",
        "Deviation"
      ],
      "fields": {
        "Average": {
          "type": "number",
          "unit": "°F",
          "value": 70,
          "lowerBound": -200,
          "upperBound": 200
        },
        "Deviation": {
          "type": "number",
          "unit": "°F",
          "value": 0.2,
          "lowerBound": 0,
          "upperBound": 5
        }
      }
    }"""
  ) extends ElementSeed {
    def this(seedData: Map[String, String]) {
      this(
        average = seedData("Average").toInt,
        deviation = seedData("Deviation").toDouble
      )
    }
    def randomDeviation(mean: Double, scale: Double): Double = {
      val lowerBound = mean - deviation
      val upperBound = mean + deviation
      randomDouble(lowerBound, upperBound)
    }
    def buildLayer(height: Int, width: Int, scale: Double): Layer = {
      val layer: Layer = new Layer(AB.fill(height)(AB.fill(width)(None)))
      for {
        x <- 0 until height
        y <- 0 until width
      } {
        val value = randomDeviation(average, scale)
        layer.setElement(x, y, new Temperature(value))
      }
      layer.smoothLayer(3, 3)
      return layer
    }
  }
}
