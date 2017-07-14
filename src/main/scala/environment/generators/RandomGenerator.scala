package environment.generator

import environment._
import environment.cell._
import environment.element._
import environment.generator.LayerGenerator._

import customtypes.Grid._

import scala.util.Random
import scala.collection.mutable.{ArrayBuffer => AB}



object RandomGenerator {

  def buildRandomEnvironment(name: String, length: Int, width: Int, scarcityMap: Map[String,Option[Double]]): Environment = {
    val grid = initializedGrid(length, width)
    val environment = new Environment(name, grid)
    for ((element, scarcity) <- scarcityMap) {
      val layer = generateLayer(element, scarcity)
      environment.setLayer(layer)
    }
    return environment
  }

  def initializedGrid(length: Int, width: Int): Grid[Cell] = {
    val grid: Grid[Cell] = AB.fill(length)(AB.fill(width)(None))
    for {
      x <- 0 until length
      y <- 0 until width
    } grid(x)(y) = Some(Cell(x, y))
    return grid
  }





  // def populateElements(scarcityMap: Map[String,Double]): AB[Element] = {
  //   val elements: AB[Element] = (for {
  //     (element, scarcity) <- scarcityMap
  //     if Random.nextDouble <= scarcity
  //   } yield createElement(element)).to[AB]
  //   return elements
  // }
  //
  // def createElement(elementName: String): Element = elementName match {
  //   case "Elevation"    => val v = new Elevation(); v.random; v
  //   case "Latitude"     => val v = new Latitude(); v.random; v
  //   case "Longitude"    => val v = new Longitude(); v.random; v
  //   case "Temperature"  => val v = new Temperature(); v.random; v
  //   case "Wind Speed"   => val v = new WindSpeed(); v.random; v
  // }

}
