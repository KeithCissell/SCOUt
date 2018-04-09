package environment.element

import scoututil.Util._
import environment.layer._
import environment.element._
import environment.element.seed._

import scala.collection.mutable.{ArrayBuffer => AB}


class WaterDepth(var value: Option[Double]) extends Element {
  val name = "Water Depth"
  val unit = "ft"
  val constant = false
  val radial = false
  val lowerBound = 0.0
  val upperBound = 100.0
  def this(d: Double) = this(Some(d))
  def this()          = this(None)
}

// package seed {
//   case class WaterDepthSeed(
//     val elementName: String = "Water Depth",
//     val dynamic: Boolean = true,
//     val average: Double = 0.0,
//     val deviation: Double = 0.2,
//     val formFields: String = """{
//       "field-keys": [
//       ],
//       "fields": {
//       }
//     }"""
//   ) extends ElementSeed {
//   }
// }
