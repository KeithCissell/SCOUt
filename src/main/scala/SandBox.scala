// src\main\scala\SandBox.scala
import environment._
import environment.point._
import environment.variable._
import environment.Builder._

import customtypes.Grid._

import scala.collection.mutable.{ArrayBuffer => AB}


object SandBox {

  def main(args: Array[String]) = {
    /*******************************************************
    ** Create data to test on
    *******************************************************/

    // Variable
    val height1 = new Height(Some(417.0))
    val latitude1 = new Latitude()
    val longitude1 = new Longitude(245.5)
    val temperature1 = new Temperature(77.0)
    val windSpeed1 = new WindSpeed(0)
    val variableList1 = AB(
      height1, latitude1, longitude1, temperature1, windSpeed1
    )

    val height2 = new Height(17)
    val latitude2 = new Latitude(23.45)
    val longitude2 = new Longitude(200.0)
    val temperature2 = new Temperature(100)
    val windSpeed2 = new WindSpeed(15)
    val variableList2 = AB(
      height2, latitude2, longitude2, temperature2, windSpeed2
    )

    // Point
    val point11 = new Point(1, 1, variableList1)
    val point12 = new Point(1, 2, variableList2)
    val point13 = new Point(1, 3)
    val point21 = new Point(2, 1)
    val point22 = new Point(2, 2)
    val point23 = new Point(2, 3)
    val point31 = new Point(3, 1)
    val point32 = new Point(3, 2)
    val point33 = new Point(3, 3)

    // Environment
    val row1: AB[Option[Point]] = AB(Some(point11), Some(point12), Some(point13))
    val row2: AB[Option[Point]] = AB(Some(point21), Some(point22), Some(point23))
    val row3: AB[Option[Point]] = AB(Some(point31), Some(point32), Some(point33))
    val grid: Grid[Point] = AB(row1, row2, row3)
    val environment = new Environment("Test", grid)
    val emptyEnv = new Environment("Empty")

    // EnvironmentBuilder
    val scarcityMap1 = Map("Height" -> 1.0, "Temperature" -> 0.5, "Latitude" -> 0.25)
    val testBuild = buildRandomGrid(3, 3, scarcityMap1)

    // Other
    val allVariableTypes = AB(
      height1, latitude1, longitude1, temperature1, windSpeed1
    )
    val testLayer = AB(
      AB(Some(height1), Some(height2), None),
      AB(None, None, None),
      AB(None, None, None)
    )


    /*******************************************************
    ** Play with data
    *******************************************************/

    // Test getCluster
    // println(environment.getPoint(1, 1))
    // println(environment.getPoint(-3, 10))
    // println(environment.getCluster(2, 2, 1))
    // println(environment.getCluster(2, 2, 5))

    // Test buildRandomGrid
    println(testBuild)


  }

}
