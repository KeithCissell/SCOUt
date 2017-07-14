// src\main\scala\SandBox.scala
import environment._
import environment.cell._
import environment.variable._
import environment.RandomGenerator._

import customtypes.Grid._

import scala.collection.mutable.{ArrayBuffer => AB}


object SandBox {

  def main(args: Array[String]) = {
    /*******************************************************
    ** Create data to test on
    *******************************************************/

    // Variable
    val elevation1 = new Elevation(Some(417.0))
    val latitude1 = new Latitude()
    val longitude1 = new Longitude(245.5)
    val temperature1 = new Temperature(77.0)
    val windSpeed1 = new WindSpeed(0)
    val variableList1 = AB(
      elevation1, latitude1, longitude1, temperature1, windSpeed1
    )

    val elevation2 = new Elevation(17)
    val latitude2 = new Latitude(23.45)
    val longitude2 = new Longitude(200.0)
    val temperature2 = new Temperature(100)
    val windSpeed2 = new WindSpeed(15)
    val variableList2 = AB(
      elevation2, latitude2, longitude2, temperature2, windSpeed2
    )

    // Cell
    val cell11 = new Cell(1, 1, variableList1)
    val cell12 = new Cell(1, 2, variableList2)
    val cell13 = new Cell(1, 3)
    val cell21 = new Cell(2, 1)
    val cell22 = new Cell(2, 2)
    val cell23 = new Cell(2, 3)
    val cell31 = new Cell(3, 1)
    val cell32 = new Cell(3, 2)
    val cell33 = new Cell(3, 3)

    // Environment
    val row1: AB[Option[Cell]] = AB(Some(cell11), Some(cell12), Some(cell13))
    val row2: AB[Option[Cell]] = AB(Some(cell21), Some(cell22), Some(cell23))
    val row3: AB[Option[Cell]] = AB(Some(cell31), Some(cell32), Some(cell33))
    val grid: Grid[Cell] = AB(row1, row2, row3)
    val environment = new Environment("Test", grid)
    val emptyEnv = new Environment("Empty")

    // EnvironmentBuilder
    val scarcityMap1 = Map("Elevation" -> 1.0, "Temperature" -> 0.5, "Latitude" -> 0.25)
    val testBuild = buildRandomGrid(3, 3, scarcityMap1)

    // Other
    val allVariableTypes = AB(
      elevation1, latitude1, longitude1, temperature1, windSpeed1
    )
    val testLayer = AB(
      AB(Some(elevation1), Some(elevation2), None),
      AB(None, None, None),
      AB(None, None, None)
    )


    /*******************************************************
    ** Play with data
    *******************************************************/

    // Test getCluster
    // println(environment.getCell(1, 1))
    // println(environment.getCell(-3, 10))
    // println(environment.getCluster(2, 2, 1))
    // println(environment.getCluster(2, 2, 5))

    // Test buildRandomGrid
    println(testBuild)


  }

}
