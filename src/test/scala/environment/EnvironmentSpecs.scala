// src\test\scala\environment\EnvironmentSpecs.scala
import environment._
import environment.cell._
import environment.variable._

import customtypes.Grid._

import org.specs2.mutable.Specification
import scala.collection.mutable.{ArrayBuffer => AB, Map => MM}

object EnvironmentSpecs extends Specification {

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

  // Cell
  val point11 = new Cell(1, 1, variableList1)
  val point12 = new Cell(1, 2, variableList2)
  val point13 = new Cell(1, 3)
  val point21 = new Cell(2, 1)
  val point22 = new Cell(2, 2)
  val point23 = new Cell(2, 3)
  val point31 = new Cell(3, 1)
  val point32 = new Cell(3, 2)
  val point33 = new Cell(3, 3)

  // Environment
  val row1: AB[Option[Cell]] = AB(Some(point11), Some(point12), Some(point13))
  val row2: AB[Option[Cell]] = AB(Some(point21), Some(point22), Some(point23))
  val row3: AB[Option[Cell]] = AB(Some(point31), Some(point32), Some(point33))
  val grid: Grid[Cell] = AB(row1, row2, row3)
  val environment = new Environment("Test", grid)
  val emptyEnv = new Environment("Empty")

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
  ** Specs2 Tests
  *******************************************************/

  // Variable Tests
  "\nVariable classes hold environmental information and" should {

    "Properly construct" in {
      (height1.value == Some(417.0)) &&
      (temperature1.value == Some(77.0)) &&
      (latitude1.value == None)
    }
    step(height1.set(0.0))
    step(temperature1.set(85.0))
    step(latitude1.set(123.45))
    "Allow uninitialized or inconstant value to be set" in {
      (height1.value == Some(417.0)) &&
      (temperature1.value == Some(85.0)) &&
      (latitude1.value == Some(123.45))
    }
  }
  // Cell Tests
  "\nCell class holds (x,y) position and list of Variables and" should {

    "Properly construct" in {
      (point11.variables == variableList1) &&
      (point13.variables == AB.empty)
    }
    "Get x position" in {
      point11.getX == 1
    }
    "Get y position" in {
      point11.getY == 1
    }
    "Calculate distance to another Cell" in {
      point11.dist(point12) == 1.0
    }
  }
  // Environment Tests
  "\nEnvironment class holds a grid of Cells and" should {

    "Properly construct" in {
      (environment.grid == grid) &&
      (emptyEnv.grid == AB(AB(None)))
    }
    "Return a Cell from (x,y)" in {
      environment.getCell(1, 1) == Some(point11)
    }
    "Return a Set of all varialbe names on the grid" in {
      environment.getVariableNames == allVariableTypes.map(_.name).toSet
    }
    "Return a grid layer of a specific Variable" in {
      environment.getLayer("Height") == testLayer
    }
  }

  // step(println(s"\n\n${}\n\n"))
  // step(println(s"\n\n${}\n\n"))


}
