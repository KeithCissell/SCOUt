// src\main\scala\environment\EnvironmentBuilder.scala
package environment

import environment._
import environment.point._
import environment.variable._

import customtypes.Grid._

import scala.util.Random
import scala.collection.mutable.{ArrayBuffer => AB}



object Builder {

  case class VariableScarcity(var scarcityMap: Map[String,Double] = Map.empty)

  def buildRandomGrid(length: Int, width: Int, scarcityMap: Map[String,Double]): Grid[Point] = {
    val grid: Grid[Point] = AB.fill(length)(AB.fill(width)(None))
    for {
      x <- 0 until length
      y <- 0 until width
    } grid(x)(y) = Some(Point(x, y, populateVariables(scarcityMap)))
    return grid
  }

  def populateVariables(scarcityMap: Map[String,Double]): AB[Variable] = {
    val variables: AB[Variable] = (for {
      (variable, scarcity) <- scarcityMap
      if Random.nextDouble <= scarcity
    } yield createVariable(variable)).to[AB]
    return variables
  }

  def createVariable(variableName: String): Variable = variableName match {
    case "Height"       => val v = new Height(); v.random; v
    case "Latitude"     => val v = new Latitude(); v.random; v
    case "Longitude"    => val v = new Longitude(); v.random; v
    case "Temperature"  => val v = new Temperature(); v.random; v
    case "Wind Speed"   => val v = new WindSpeed(); v.random; v
  }

}
