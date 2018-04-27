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
    //------------------------------------------------------------------------
    val terrainModifications: List[TerrainModification] = List(
      // ElevationModification(modification = 150.0, deviation = 20.0, coverage = 0.3, slope = 6.0),
      // ElevationModification(modification = -30.0, deviation = 3.0, coverage = 0.17, slope = 10.0),
      // WaterPoolModification(maxDepth = 10.0, deviation = 2.5, coverage = 0.15, slope = 3.0),
      WaterStreamModification(depth = 5.0, deviation = 2.0, width = 30.0, length = 1000.0)
    )
    //------------------------------------------------------------------------
    val terrainLayers: List[Layer] = generateTerrain(height, width, scale, terrainModifications)
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


  def generateTerrain(height: Int, width: Int, scale: Double, modifications: List[TerrainModification]): List[Layer] = {
    // params TEMP
    //------------------------------------------------------------------------
    val rootValue = 0.0
    val baseDeviation = 3.0
    //------------------------------------------------------------------------

    // CREATE CONSTRUCTION LAYER
    val constructionLayer = new ConstructionLayer(height, width, scale)

    // INITIALIZE ELEVATION LAYER
    val elevation: Layer = new Layer(AB.fill(height)(AB.fill(width)(None)))
    for {
      x <- 0 until height
      y <- 0 until width
    } {
      val randomElevation = randomDouble(rootValue - baseDeviation, rootValue + baseDeviation)
      elevation.setElement(x, y, new Elevation(randomElevation))
    }
    elevation.smoothLayer(3, 3)

    // INITIALIZE WATERDEPTH LAYER
    val waterDepth: Layer = new Layer(AB.fill(height)(AB.fill(width)(Some(new WaterDepth(0.0)))))

    // APPLY MODIFICATIONS
    for (mod <- modifications) mod match {
      case mod: ElevationModification => modifyElevation(mod, elevation, constructionLayer)
      case mod: WaterPoolModification => modifyWaterPool(mod, waterDepth, constructionLayer)
      case mod: WaterStreamModification => modifyWaterStream(mod, waterDepth, constructionLayer)
    }

    // Return the constructed layers
    return List(elevation, waterDepth)
  }


  // Elevation Modification
  // Shape mountains/hills/valleys (smooth along the way)
  def modifyElevation(mod: ElevationModification, elevation: Layer, constructionLayer: ConstructionLayer) = constructionLayer.getRandomUnmodified() match {
    case None => // No unmodified cells
    case Some(startCell) => {
      // Set local variables
      var modifiedCells: AB[(Int,Int)] = AB()
      val numCellsToMod = Math.round(mod.coverage * constructionLayer.cellCount).toInt
      // Initial modification
      val startX = startCell._1
      val startY = startCell._2
      elevation.setElementValue(startX, startY, mod.modification)
      constructionLayer.setToModified(startX, startY, "elevation")
      modifiedCells.append((startX, startY))
      // Move to random, unmodified neighbors and modify
      for (i <- 0 until numCellsToMod) constructionLayer.getNextUnmodifiedNeighbor(modifiedCells) match {
        case None => // No neighbor cells to modify
        case Some((x,y)) => {
          val currentValue = elevation.getElementValue(x, y).getOrElse(0.0)
          val modification = randomDouble((mod.modification - mod.deviation), (mod.modification + mod.deviation))
          val newValue = currentValue + modification
          elevation.setElementValue(x, y, newValue)
          constructionLayer.setToModified(x, y, "elevation")
          modifiedCells.append((x, y))
        }
      }
      // Apply sloping factor to modified area through smoothing
      val effectedRadius = Math.abs(Math.round(mod.modification / mod.slope).toInt)
      for (i <- 0 until modifiedCells.length) {
        val randomIndex = randomInt(0, modifiedCells.length - 1)
        val c = modifiedCells.remove(randomIndex)
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
  }


  // Water Pool Modification
  // Erodes area in a "step-down" erosion approach based on the given slope and maxDepth
  def modifyWaterPool(mod: WaterPoolModification, waterDepth: Layer, constructionLayer: ConstructionLayer) = constructionLayer.getRandomUnmodified() match {
    case None => // No unmodified cells
    case Some(startCell) => {
      // Set local variables
      var modifiedCells: AB[(Int,Int)] = AB()
      val numCellsToMod = Math.round(mod.coverage * constructionLayer.cellCount).toInt
      val stepDepth = constructionLayer.scale / mod.slope
      val numSteps = Math.floor(mod.maxDepth / stepDepth).toInt + 1
      val steps: List[Double] = (for (i <- 0 until numSteps) yield i * stepDepth).toList :+ mod.maxDepth
      // Initial modification
      val startX = startCell._1
      val startY = startCell._2
      waterDepth.setElementValue(startX, startY, steps(0))
      constructionLayer.setToModified(startX, startY, "waterDepth")
      modifiedCells.append((startX, startY))
      // Move to random, unmodified neighbors and modify
      for (i <- 0 until numCellsToMod) constructionLayer.getNextUnmodifiedNeighbor(modifiedCells) match {
        case None => // No neighbor cells to modify
        case Some((x,y)) => {
          val newValue = randomDouble((steps(0) - mod.deviation), (steps(0) + mod.deviation))
          waterDepth.setElementValue(x, y, newValue)
          constructionLayer.setToModified(x, y, "waterDepth")
          modifiedCells.append((x, y))
        }
      }
      // Erode each step
      for (i <- 1 until numSteps) {
        val step = steps(i)
        val validThreshold = step - i * mod.deviation
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
            val newValue = currentDepth + randomDouble((step - mod.deviation), (step + mod.deviation))
            waterDepth.setElementValue(currentX, currentY, newValue)
          }
        }
      }
      // Smooth modified area
      waterDepth.smoothArea(modifiedCells, 3, 3)
    }
  }


  // Water Stream Modification
  // Erode channels of water with a directional influence
  def modifyWaterStream(mod: WaterStreamModification, waterDepth: Layer, constructionLayer: ConstructionLayer) = constructionLayer.getRandomUnmodified() match {
    case None => // No unmodified cells
    case Some(startCell) => {
      // Set local variables
      var modifiedCells: AB[(Int,Int)] = AB()
      val modCellLength = Math.ceil(mod.length / constructionLayer.scale).toInt
      val modCellWidth = Math.ceil(mod.width / constructionLayer.scale).toInt - 1
      // Initialize starting point of stream
      var currentX = startCell._1
      var currentY = startCell._2
      waterDepth.setElementValue(currentX, currentY, mod.depth)
      constructionLayer.setToModified(currentX, currentY, "waterDepth")
      modifiedCells.append((currentX, currentY))
      // Set directional influence for the stream to move
      val directionalInfluence = constructionLayer.getDirectionToUnmodified(currentX, currentY).getOrElse(0.0)
      // Move in direction for as long as possible
      var i = 1
      var done = false
      while (i < modCellLength && !done) constructionLayer.getUnmodifiedNeighborDirectional(currentX, currentY, directionalInfluence, 181) match {
        case Some(c) => {
          currentX = c._1
          currentY = c._2
          val newValue = mod.depth + randomDouble((mod.depth - mod.deviation), (mod.depth + mod.deviation))
          waterDepth.setElementValue(currentX, currentY, newValue)
          constructionLayer.setToModified(currentX, currentY, "waterDepth")
          modifiedCells.append((currentX, currentY))
          i += 1
        }
        case None => done = true
      }
      // Widen the stream created
      for (cell <- modifiedCells) {
        // find number of cells to widen in each direction
        val modLeft = Math.floor(modCellWidth / 2) + randomInt(0, 1)
        val modRight = modCellWidth - modLeft
        // Mod to the left
        val directionalLeft = normalizeDegrees(directionalInfluence - 90)
        currentX = cell._1
        currentY = cell._2
        i = 0
        done = false
        while (i < modLeft && !done) constructionLayer.getUnmodifiedNeighborDirectional(currentX, currentY, directionalLeft, 46) match {
          case Some(c) => {
            currentX = c._1
            currentY = c._2
            val newValue = mod.depth + randomDouble((mod.depth - mod.deviation), (mod.depth + mod.deviation))
            waterDepth.setElementValue(currentX, currentY, newValue)
            constructionLayer.setToModified(currentX, currentY, "waterDepth")
            modifiedCells.append((currentX, currentY))
            i += 1
          }
          case None => done = true
        }
        // Mod to the right
        val directionalRight = normalizeDegrees(directionalInfluence + 90)
        currentX = cell._1
        currentY = cell._2
        i = 0
        done = false
        while (i < modRight && !done) constructionLayer.getUnmodifiedNeighborDirectional(currentX, currentY, directionalRight, 46) match {
          case Some(c) => {
            currentX = c._1
            currentY = c._2
            val newValue = mod.depth + randomDouble((mod.depth - mod.deviation), (mod.depth + mod.deviation))
            waterDepth.setElementValue(currentX, currentY, newValue)
            constructionLayer.setToModified(currentX, currentY, "waterDepth")
            modifiedCells.append((currentX, currentY))
            i += 1
          }
          case None => done = true
        }
      }
      // Smooth the stream
      waterDepth.smoothAreaNeighbors(modifiedCells, 3, 3)
    }
  }

}
