package environment.generator

import environment.element._

import customtypes.Grid._

import scala.collection.mutable.{ArrayBuffer => AB}

object LayerGenerator {

  def generateLayer(element: String, scarcity: Option[Double]): Grid[Element] = element match {
    case "Decible"      => decibleLayer
    case "Elevation"    => elevationLayer
    case "Latitude"     => latitudeLayer
    case "Longitude"    => longitudeLayer
    case "Temperature"  => temperatureLayer
    case "Wind Speed"   => windSpeedLayer
  }

  def decibleLayer: Grid[Element] = {
    AB(AB(None))
  }

  def elevationLayer: Grid[Element] = {
    AB(AB(None))
  }

  def latitudeLayer: Grid[Element] = {
    AB(AB(None))
  }

  def longitudeLayer: Grid[Element] = {
    AB(AB(None))
  }

  def temperatureLayer: Grid[Element] = {
    AB(AB(None))
  }

  def windSpeedLayer: Grid[Element] = {
    AB(AB(None))
  }

}
