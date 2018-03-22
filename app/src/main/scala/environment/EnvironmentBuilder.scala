package environment

import environment._
import environment.cell._
import environment.element._
import environment.element.seed._
import environment.layer._
import environment.terrain._

import scoututil.Util._

import scala.util.Random
import scala.collection.mutable.{ArrayBuffer => AB}


object EnvironmentBuilder {

  def buildEnvironment(
      name: String,
      height: Int,
      width: Int,
      scale: Double = 1.0,
      seeds: List[ElementSeed] = Nil): Environment = {

    // Initialize environment
    val environment = new Environment(name, height, width, scale)

    // Generate Terrain
    val elevation = generateTerrain(height, width, scale)
    environment.setLayer(elevation)

    // Place Anomolies

    // Calculate measurements
    for (seed <- seeds) {
      val layer = seed.buildLayer(height, width, scale)
      environment.setLayer(layer)
    }

    // return the generated Environment
    return environment
  }


  def generateTerrain(height: Int, width: Int, scale: Double): Layer = {
    // params TEMP
    //
    val rootValue = 0.0
    val deviation = 3.0
    // val smoothness = 0.8
    //

    // Variables
    val cellCount = height * width

    // Initialize layers
    val terrain: Grid[Terrain] = AB.fill(height)(AB.fill(width)(None))
    val elevation: Layer = new Layer(AB.fill(height)(AB.fill(width)(None)))

    // randomize landscape
    for {
      x <- 0 until height
      y <- 0 until width
    } {
      val randomElevation = randomDouble(rootValue - deviation, rootValue + deviation)
      elevation.setElement(x, y, new Elevation(randomElevation))
    }

    // Smooth Landscape
    val neighborhoodRadius = 3
    val originWeight = 3

    var pool: AB[(Int,Int)] = elevation.coordinatePool.clone()
    for (i <- 1 to cellCount) {
      // Randomly select an origin to smooth from
      val randomIndex = randomInt(0, pool.length - 1)
      val cellCoordinates = pool.remove(randomIndex)
      val x = cellCoordinates._1
      val y = cellCoordinates._2
      elevation.smooth(x, y, neighborhoodRadius, originWeight)
    }


    // shape mountains/hills/valleys (smooth along way)
    val modifications: List[TerrainModification] = List(
      TerrainModification(modification = 50.0, coverage = 0.3, slope = 30.0),
      TerrainModification(modification = -30.0, coverage = 0.15, slope = 10.0)
    )

    var unmodifiedCells: AB[(Int,Int)] = elevation.coordinatePool.clone()
    var modifiedCells: AB[(Int,Int,Double)] = AB()

    for (mod <- modifications) {
      val randomIndex = randomInt(0, unmodifiedCells.length - 1)
      val cellCoordinates = unmodifiedCells.remove(randomIndex)
      var currentX = cellCoordinates._1
      var currentY = cellCoordinates._2
      var currentValue = elevation.getElementValue(currentX, currentY).getOrElse(0.0)
      val numCellsToMod = Math.round(mod.coverage * cellCount).toInt

      // Initial modification
      elevation.setElementValue(currentX, currentY, mod.modification)
      modifiedCells.append((currentX, currentY, mod.slope))

      // Move to random, unmodified neighbors and modify
      for (i <- 0 until numCellsToMod) {
        // Select a new, unmodified neighbor
        currentX = randomInt(Math.max(0, currentX - 1), Math.min(currentX + 1, height - 1))
        currentY = randomInt(Math.max(0, currentY - 1), Math.min(currentY + 1, width - 1))
        currentValue = elevation.getElementValue(currentX, currentY).getOrElse(0.0)
        // ensure modifications don't overlap
        if (unmodifiedCells.contains((currentX,currentY))) {
          val newValue = randomDouble((mod.modification - deviation), (mod.modification + deviation))
          elevation.setElementValue(currentX, currentY, newValue)
          unmodifiedCells -= ((currentX, currentY))
          modifiedCells.append((currentX, currentY, mod.slope))
        }
      }

      // Apply sloping factor to surrounding, unmodifiedCells
      val effectedRadius = Math.abs(Math.round(mod.modification / mod.slope).toInt)
      for (c <- modifiedCells) {
        val originX = c._1
        val originY = c._2
        for {
          x <- (originX - effectedRadius) to (originX + effectedRadius)
          y <- (originY - effectedRadius) to (originY + effectedRadius)
          if dist(x, y, originX, originY) != 0
          if dist(x, y, originX, originY) <= effectedRadius
        } {
          elevation.smooth(x, y, 2, dist(originX, originY, x, y))
        }
      }
      modifiedCells = AB() // clear array
    }

    // erode areas for water






    return elevation
  }

}
