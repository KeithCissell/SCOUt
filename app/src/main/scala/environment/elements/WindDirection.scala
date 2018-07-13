package environment.element

import scoututil.Util._
import environment.layer._
import environment.element._
import environment.element.seed._

import scala.collection.mutable.{ArrayBuffer => AB}


class WindDirection(var value: Option[Double]) extends Element {
  val name = "Wind Direction"
  val unit = "° from N"
  val constant = false
  val radial = true
  val lowerBound = 0.0
  val upperBound = 360.0
  def this(d: Double) = this(Some(d))
  def this()          = this(None)
}

package seed {
  case class WindDirectionSeed(
    val elementName: String = "Wind Direction",
    val dynamic: Boolean = true,
    val average: Double = 0.0,
    val deviation: Double = 1.5,
    val formFields: String = """{
      "field-keys": [
        "Average",
        "Deviation"
      ],
      "fields": {
        "Average": {
          "type": "number",
          "unit": "° from N",
          "value": 0,
          "lowerBound": 0,
          "upperBound": 360
        },
        "Deviation": {
          "type": "number",
          "unit": "°",
          "value": 1.5,
          "lowerBound": 0,
          "upperBound": 25
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
    def randomDeviation(mean: Double): Double = {
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
        val value = randomDeviation(average)
        layer.setElement(x, y, new WindDirection(value))
      }
      layer.smoothLayer(3, 3)
      return layer
    }
  }
}
