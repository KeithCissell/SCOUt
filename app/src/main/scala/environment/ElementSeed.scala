package environment.element.seed

import scoututil.Util._
import environment.layer._
import environment.element._

import scala.math._
import scala.util.Random


trait ElementSeed {
  val elementName: String
  val dynamic: Boolean
  val formFields: String

  def buildLayer(height: Int, width: Int, scale: Double): Layer
}

// Gives access to all the avialable element types that can be seed generated
object ElementSeedList {
  // List of all seeds set to default
  def defaultSeedList(): List[ElementSeed] = List(
    new DecibelSeed(),
    new ElevationSeed(),
    new LatitudeSeed(),
    new LongitudeSeed(),
    new TemperatureSeed(),
    new WaterDepthSeed(),
    new WindDirectionSeed(),
    new WindSpeedSeed()
  )

  // Returns the form field for the requested element type
  def getSeedForm(elementType: String): String = elementType match {
    case "Decibel"        => DecibelSeed().formFields
    case "Elevation"      => ElevationSeed().formFields
    case "Latitude"       => LatitudeSeed().formFields
    case "Longitude"      => LongitudeSeed().formFields
    case "Temperature"    => TemperatureSeed().formFields
    case "Water Depth"    => WaterDepthSeed().formFields
    case "Wind Direction" => WindDirectionSeed().formFields
    case "Wind Speed"     => WindSpeedSeed().formFields
  }
}
