package environment.element.seed

import scoututil.Util._
import environment.layer._
import environment.element._

import scala.math._
import scala.util.Random


// List of all seed defaults
object SeedList {
  val defaultSeedList: List[ElementSeed] = List(
    DecibelSeed(),
    ElevationSeed(),
    LatitudeSeed(),
    LongitudeSeed(),
    TemperatureSeed(),
    WindDirectionSeed(),
    WindSpeedSeed()
  )

  def getSeedForm(elementType: String): String = elementType match {
    case "Decibel"        => "{}"//DecibelSeed().formFields
    case "Elevation"      => ElevationSeed().formFields
    case "Latitude"       => LatitudeSeed().formFields
    case "Longitude"      => LongitudeSeed().formFields
    case "Temperature"    => TemperatureSeed().formFields
    case "Wind Direction" => WindDirectionSeed().formFields
    case "Wind Speed"     => WindSpeedSeed().formFields
  }
}

trait ElementSeed {
  val elementName: String
  val dynamic: Boolean
  // val formFields: String
  // def this(seedData: Map[String, String])
  def buildLayer(height: Int, width: Int, scale: Double): Layer
}
