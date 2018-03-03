package environment.element

import scoututil.Util._
import environment.layer._
import environment.element._
import environment.element.seed._

import scala.collection.mutable.{ArrayBuffer => AB}


class Latitude(var value: Option[Double]) extends Element {
  val name = "Latitude"
  val unit = "°"
  val constant = true
  val circular = true
  val lowerBound = -90.0
  val upperBound = 90.0
  def this(d: Double) = this(Some(d))
  def this()          = this(None)
}

package seed {
  case class LatitudeSeed(
    val elementName: String = "Latitude",
    val dynamic: Boolean = false,
    val rootValue: Double = 1.0,
    val formFields: String = """{
      "field-keys": [
      "Root Value"
      ],
      "fields": {
        "Root Value": {
          "type": "number",
          "unit": "°",
          "value": 1.0,
          "lowerBound": 0,
          "upperBound": 360
        }
      }
    }"""
  ) extends ElementSeed {
    def this(seedData: Map[String, String]) {
      this(
        rootValue = seedData("Root Value").toDouble
      )
    }
    def buildLayer(height: Int, width: Int, scale: Double): Layer = {
      val layer = new Layer(AB.fill(height)(AB.fill(width)(None)))
      for {
        x <- 0 until height
        y <- 0 until width
      } {
        val value = rootValue + (y * .000003 * scale)
        layer.setElement(x, y, new Latitude(value))
      }
      return layer
    }
  }
}
