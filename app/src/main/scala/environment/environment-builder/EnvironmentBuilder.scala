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
    val terrainLayers: List[Layer] = generateTerrain(height, width, scale)
    for (layer <- terrainLayers) environment.setLayer(layer)

    // Place Anomolies

    // Calculate measurements
    for (seed <- seeds) {
      val layer = seed.buildLayer(height, width, scale)
      environment.setLayer(layer)
    }

    // return the generated Environment
    return environment
  }


  def generateTerrain(height: Int, width: Int, scale: Double): List[Layer] = {
    // params TEMP
    //------------------------------------------------------------------------
    val rootValue = 0.0
    val deviation = 3.0
    // val smoothness = 0.8
    //------------------------------------------------------------------------

    // SET LOCAL VARIABLES
    val cellCount = height * width
    val envArea = cellCount * Math.pow(scale, 2)
    val constructionLayer = new ConstructionLayer(height, width)

    // ELEVATION LAYER
    // shape mountains/hills/valleys (smooth along the way)
    //------------------------------------------------------------------------
    val elevationModifications: List[ElevationModification] = List(
      ElevationModification(modification = 150.0, coverage = 0.3, slope = 6.0),
      ElevationModification(modification = -30.0, coverage = 0.17, slope = 10.0)
    )
    //------------------------------------------------------------------------
    // Initialize Elevation Layer
    val elevation: Layer = new Layer(AB.fill(height)(AB.fill(width)(None)))
    for {
      x <- 0 until height
      y <- 0 until width
    } {
      val randomElevation = randomDouble(rootValue - deviation, rootValue + deviation)
      elevation.setElement(x, y, new Elevation(randomElevation))
    }
    // Smooth Elevation
    val neighborhoodRadius = 3
    val originWeight = 3
    elevation.smoothLayer(neighborhoodRadius, originWeight)
    // Apply Elivation Modifications
    for (mod <- elevationModifications) {
      var modifiedCells: AB[(Int,Int)] = AB()
      val numCellsToMod = Math.round(mod.coverage * cellCount).toInt
      // Initial modification
      constructionLayer.getRandomUnmodified() match {
        case Some(startCell) => {
          val startX = startCell._1
          val startY = startCell._2
          elevation.setElementValue(startX, startY, mod.modification)
          constructionLayer.setToModified(startX, startY)
          modifiedCells.append((startX, startY))
        }
        case None => // No unmodified cells found
      }

      // Move to random, unmodified neighbors and modify
      for (i <- 0 until numCellsToMod) constructionLayer.getNextUnmodifiedNeighbor(modifiedCells) match {
        case Some((x,y)) => {
          val currentValue = elevation.getElementValue(x, y).getOrElse(0.0)
          val modification = randomDouble((mod.modification - deviation), (mod.modification + deviation))
          val newValue = currentValue + modification
          elevation.setElementValue(x, y, newValue)
          constructionLayer.setToModified(x, y)
          modifiedCells.append((x, y))
        }
        case None => // No neighbor cells to modify
      }
      // Smooth modified area multiple times (to avoid initial averaging flaws)
      // val smooth = 2
      // val radius = 2
      // val weight = 2
      // for (x <- 0 until smooth) elevation.smoothArea(modifiedCells, radius, weight)
      // Apply sloping factor to modified area through smoothing
      val effectedRadius = Math.abs(Math.round(mod.modification / mod.slope).toInt)
      for (c <- modifiedCells) {
        val originX = c._1
        val originY = c._2
        for {
          x <- (originX - effectedRadius) to (originX + effectedRadius)
          y <- (originY - effectedRadius) to (originY + effectedRadius)
          if dist(x, y, originX, originY) != 0
          if dist(x, y, originX, originY) <= effectedRadius
        } elevation.smooth(x, y, 2, dist(originX, originY, x, y))
      }
    }

    // WATERDEPTH LAYER
    // Form pools and streams of water
    //------------------------------------------------------------------------
    val waterModifications: List[WaterModification] = List(
      WaterPoolModification(maxDepth = 10.0, coverage = 0.15, slope = 3.0),
      WaterStreamModification(depth = 5.0, width = 15.0, momentum = 10.0)
    )
    //------------------------------------------------------------------------
    // Initialize WaterDepth Layer
    val waterDepth: Layer = new Layer(AB.fill(height)(AB.fill(width)(Some(new WaterDepth(0.0)))))
    // Create list of valid starting points for streams
    // var streamStartPoints: AB[(Int,Int)] = AB()
    // for (unmodified <- unmodifiedCells) unmodified match {
    //   case (0, _)       => streamStartPoints.append(unmodified)
    //   case (height, _)  => streamStartPoints.append(unmodified)
    //   case (_, 0)       => streamStartPoints.append(unmodified)
    //   case (_, width)   => streamStartPoints.append(unmodified)
    // }
    // Apply Water Modifications
    for (mod <- waterModifications) mod match {
      // Water Pool Modification
      // Erodes area in a "step-down" erosion approach based on the given slope and maxDepth
      case mod: WaterPoolModification => {
        var modifiedCells: AB[(Int,Int)] = AB()
        val numCellsToMod = Math.round(mod.coverage * cellCount).toInt
        val stepDepth = scale / mod.slope
        val numSteps = Math.floor(mod.maxDepth / stepDepth).toInt + 1
        val steps: List[Double] = (for (i <- 0 until numSteps) yield i * stepDepth).toList :+ mod.maxDepth
        // Initial modification
        constructionLayer.getRandomUnmodified() match {
          case Some(startCell) => {
            val startX = startCell._1
            val startY = startCell._2
            waterDepth.setElementValue(startX, startY, steps(0))
            constructionLayer.setToModified(startX, startY)
            modifiedCells.append((startX, startY))
          }
          case None => // No unmodified cells found
        }
        // Move to random, unmodified neighbors and modify
        for (i <- 0 until numCellsToMod) constructionLayer.getNextUnmodifiedNeighbor(modifiedCells) match {
          case Some((x,y)) => {
            val newValue = randomDouble((steps(0) - deviation), (steps(0) + deviation))
            waterDepth.setElementValue(x, y, newValue)
            constructionLayer.setToModified(x, y)
            modifiedCells.append((x, y))
          }
          case None => // No neighbor cells to modify {
        }
        // Erode each step
        for (i <- 1 until numSteps) {
          val step = steps(i)
          val validThreshold = step - i * deviation
          // Erode non-border cells
          for (cell <- modifiedCells) {
            var shouldErode = true
            val currentX = cell._1
            val currentY = cell._2
            val currentDepth = waterDepth.getElementValue(currentX, currentY).getOrElse(0.0)
            // Check if cell is on border
            for {
              x <- (currentX - 1) to (currentX + 1)
              y <- (currentY - 1) to (currentY + 1)
              if currentDepth < validThreshold
            } shouldErode = false
            // Erode
            if (shouldErode) {
              val newValue = currentDepth + randomDouble((step - deviation), (step + deviation))
              waterDepth.setElementValue(currentX, currentY, newValue)
            }
          }
        }
        // Smooth modified area
        val radius = 3
        val weight = 3
        waterDepth.smoothArea(modifiedCells, radius, weight)
      }
      // Water Stream Modification
      // Erode channels of water with a directional influence
      case mod: WaterStreamModification => {

      }
    }





    return List(elevation, waterDepth)
  }

}
