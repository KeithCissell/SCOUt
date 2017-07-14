import environment._
import environment.cell._
import environment.element._

import customtypes.Grid._

import org.specs2.mutable.Specification
import scala.collection.mutable.{ArrayBuffer => AB, Map => MM}

object EnvironmentSpecs extends Specification {

  /*******************************************************
  ** Create data to test on
  *******************************************************/

  // Element
  val elevation1 = new Elevation(Some(417.0))
  val latitude1 = new Latitude()
  val longitude1 = new Longitude(245.5)
  val temperature1 = new Temperature(77.0)
  val windSpeed1 = new WindSpeed(0)
  val elementList1 = List(
    elevation1, latitude1, longitude1, temperature1, windSpeed1
  )
  val elementMap1 = Map(
    elevation1.name -> elevation1,
    latitude1.name -> latitude1,
    longitude1.name -> longitude1,
    temperature1.name -> temperature1,
    windSpeed1.name -> windSpeed1
  )

  val elevation2 = new Elevation(17)
  val latitude2 = new Latitude(23.45)
  val longitude2 = new Longitude(200.0)
  val temperature2 = new Temperature(100)
  val windSpeed2 = new WindSpeed(15)
  val elementMap2 = Map(
    elevation2.name -> elevation2,
    latitude2.name -> latitude2,
    longitude2.name -> longitude2,
    temperature2.name -> temperature2,
    windSpeed2.name -> windSpeed2
  )

  // Cell
  val cell11 = new Cell(1, 1, elementMap1)
  val cell12 = new Cell(1, 2, elementMap2)
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

  // Other
  val allElementTypes = AB(
    elevation1, latitude1, longitude1, temperature1, windSpeed1
  )
  val testLayer = AB(
    AB(Some(elevation1), Some(elevation2), None),
    AB(None, None, None),
    AB(None, None, None)
  )



  /*******************************************************
  ** Specs2 Tests
  *******************************************************/

  // Element Tests
  "\nElement classes hold environmental information and" should {

    "Properly construct" in {
      (elevation1.value == Some(417.0)) &&
      (temperature1.value == Some(77.0)) &&
      (latitude1.value == None)
    }
    step(elevation1.set(0.0))
    step(temperature1.set(85.0))
    step(latitude1.set(123.45))
    "Allow an uninitialized or inconstant value to be set" in {
      (elevation1.value == Some(417.0)) &&
      (temperature1.value == Some(85.0)) &&
      (latitude1.value == Some(123.45))
    }
  }
  // Cell Tests
  "\nCell class holds (x,y) position and list of Elements and" should {

    "Properly construct" in {
      (cell11.getElements == elementList1) &&
      (cell13.getElements == Nil)
    }
    "Get x position" in {
      cell11.getX == 1
    }
    "Get y position" in {
      cell11.getY == 1
    }
    "Calculate distance to another Cell" in {
      cell11.dist(cell12) == 1.0
    }
  }
  // Environment Tests
  "\nEnvironment class holds a grid of Cells and" should {

    "Properly construct" in {
      environment.getGrid == grid
    }
    "Return a Cell from (x,y)" in {
      environment.getCell(1, 1) == Some(cell11)
    }
    "Return a Set of all varialbe names on the grid" in {
      environment.getElementNames == allElementTypes.map(_.name).toSet
    }
    "Return a grid layer of a specific Element" in {
      environment.getLayer("Elevation") == testLayer
    }
  }

  // step(println(s"\n\n${}\n\n"))
  // step(println(s"\n\n${}\n\n"))


}
