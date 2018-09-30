package environment.element

import io.circe._
import scoututil.Util._
import environment.layer._
import environment.element._
import environment.element.seed._

import scala.collection.mutable.{ArrayBuffer => AB}


class Elevation(var value: Option[Double]) extends Element {
  val name = "Elevation"
  val unit = "ft"
  val constant = true
  val radial = false
  val lowerBound = -500.0
  val upperBound = 500.0
  def this(d: Double) = this(Some(d))
  def this()          = this(None)
}

package seed {
  case class ElevationSeed(
    val elementName: String = "Elevation",
    val dynamic: Boolean = false,
    val average: Double = 0.0,
    val deviation: Double = 0.15,
    val formFields: String = """{
      "field-keys": [
        "Average",
        "Deviation"
      ],
      "fields": {
        "Average": {
          "type": "number",
          "unit": "ft",
          "value": 0,
          "lowerBound": -1500,
          "upperBound": 1500
        },
        "Deviation": {
          "type": "number",
          "unit": "ft",
          "value": 0.25,
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
    def randomDeviation(mean: Double, scale: Double): Double = {
      val lowerBound = mean - (deviation * scale)
      val upperBound = mean + (deviation * scale)
      randomDouble(lowerBound, upperBound)
    }
    def buildLayer(height: Int, width: Int, scale: Double): Layer = {
      val layer: Layer = new Layer(AB.fill(height)(AB.fill(width)(None)))
      for {
        x <- 0 until height
        y <- 0 until width
      } {
        val value = randomDeviation(average, scale)
        layer.setElement(x, y, new Elevation(value))
      }
      layer.smoothLayer(3, 3)
      return layer
    }
  }
}
