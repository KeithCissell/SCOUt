import environment._
import environment.cell._
import environment.layer._
import environment.element._
import environment.generator.ElementSeeds._
import environment.generator.LayerGenerator._
import environment.generator.RandomGenerator._

import jsonhandler.Encoder._
import jsonhandler.Decoder._

import customtypes.Grid._

import scala.collection.mutable.{ArrayBuffer => AB}


object SandBox {

  def main(args: Array[String]) = {
    /*******************************************************
    ** Create data to test on
    *******************************************************/

    // Element
    val elevation1 = new Elevation(Some(417.0))
    val latitude1 = new Latitude()
    val longitude1 = new Longitude(245.5)
    val temperature1 = new Temperature(77.0)
    val windSpeed1 = new WindSpeed(2.0)
    val elementMap1 = Map(
      elevation1.name -> elevation1,
      latitude1.name -> latitude1,
      longitude1.name -> longitude1,
      temperature1.name -> temperature1,
      windSpeed1.name -> windSpeed1
    )

    val elevation2 = new Elevation(17.0)
    val latitude2 = new Latitude(23.45)
    val longitude2 = new Longitude(200.0)
    val temperature2 = new Temperature(100.0)
    val windSpeed2 = new WindSpeed(15.0)
    val elementMap2 = Map(
      elevation2.name -> elevation2,
      latitude2.name -> latitude2,
      longitude2.name -> longitude2,
      temperature2.name -> temperature2,
      windSpeed2.name -> windSpeed2
    )

    // Cell
    val cell00 = new Cell(0, 0, elementMap1)
    val cell01 = new Cell(0, 1, elementMap2)
    val cell02 = new Cell(0, 2)
    val cell10 = new Cell(1, 0)
    val cell11 = new Cell(1, 1)
    val cell12 = new Cell(1, 2)
    val cell20 = new Cell(2, 0)
    val cell21 = new Cell(2, 1)
    val cell22 = new Cell(2, 2)

    // Environment
    val row1: AB[Option[Cell]] = AB(Some(cell00), Some(cell01), Some(cell02))
    val row2: AB[Option[Cell]] = AB(Some(cell10), Some(cell11), Some(cell12))
    val row3: AB[Option[Cell]] = AB(Some(cell20), Some(cell21), Some(cell22))
    val grid: Grid[Cell] = AB(row1, row2, row3)
    val environment = new Environment("Test", grid)

    // LayerGenerator
    val decSeed1 = DecibleSeed() //DecibleSeed(sources = AB(NoiseSource(2,2,27.0)))
    val decLayer1 = decibleLayer(5, 5, decSeed1)

    val elvSeed1 = ElevationSeed()
    val elvLayer1 = elevationLayer(3, 3, elvSeed1)

    val latSeed1 = LatitudeSeed()
    val latLayer1 = latitudeLayer(3, 3, latSeed1)
    val latSeed2 = LatitudeSeed(scale = 2.0)
    val latLayer2 = latitudeLayer(3, 3, latSeed2)

    val longSeed1 = LongitudeSeed()
    val longLayer1 = longitudeLayer(3, 3, longSeed1)

    val tempSeed1 = TemperatureSeed()
    val tempLayer1 = temperatureLayer(3, 3, tempSeed1)

    val wdSeed1 = WindDirectionSeed()
    val wdLayer1 = windDirectionLayer(3, 3, wdSeed1)

    val wsSeed1 = WindSpeedSeed()
    val wsLayer1 = windSpeedLayer(3, 3, wsSeed1)

    val seedList1 = List(
      decSeed1,
      elvSeed1,
      latSeed1,
      longSeed1,
      tempSeed1,
      wdSeed1,
      wsSeed1
    )

    // EnvironmentGenerator
    val randomEnv1 = generateRandomEnvironment("Random1", 2, 2, seedList1)


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
    ** Play with data
    *******************************************************/

    // Test getCluster
    // println(environment.getCell(1, 1))
    // println(environment.getCell(-3, 10))
    // println(environment.getCluster(2, 2, 1))
    // println(environment.getCluster(2, 2, 5))

    // Test generateLayer
    // println(decLayer1)
    // println(elvLayer1)
    // println(latLayer1)
    // println(latLayer2)
    // println(longLayer1)
    // println(tempLayer1)

    // Test generateRandomEnvironment
    println(randomEnv1)

    // Test Json Encoder
    // println(encodeEnvironment(randomEnv1))

  }

}
