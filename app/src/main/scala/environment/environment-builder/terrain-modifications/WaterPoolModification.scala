package environment.terrainmodification

import environment._
import environment.cell._
import environment.element._
import environment.element.seed._
import environment.layer._
import environment.terrainmodification._

import scoututil.Util._

import scala.util.Random
import scala.collection.mutable.{ArrayBuffer => AB}

// Water Pool Modification
class WaterPoolModification(
  val name: String = "Water Pool Modification",
  val elementType: String = "Water Depth",
  val maxDepth: Double,
  val deviation: Double,
  val coverage: Double,
  val slope: Double
) extends TerrainModification {
  // Erodes area in a "step-down" erosion approach based on the given slope and maxDepth
  def modify(layer: Layer, constructionLayer: ConstructionLayer) = constructionLayer.getRandomUnmodified() match {
    case None => // No unmodified cells
    case Some(startCell) => {
      // Set local variables
      var modifiedCells: AB[(Int,Int)] = AB()
      val numCellsToMod = Math.round(coverage * constructionLayer.cellCount).toInt
      val stepDepth = constructionLayer.scale / slope
      val numSteps = Math.floor(maxDepth / stepDepth).toInt + 1
      val steps: List[Double] = (for (i <- 0 until numSteps) yield i * stepDepth).toList :+ maxDepth
      // Initial modification
      val startX = startCell._1
      val startY = startCell._2
      layer.setElementValue(startX, startY, steps(0))
      constructionLayer.setToModified(startX, startY, "waterDepth")
      modifiedCells.append((startX, startY))
      // Move to random, unmodified neighbors and modify
      for (i <- 0 until numCellsToMod) constructionLayer.getNextUnmodifiedNeighbor(modifiedCells) match {
        case None => // No neighbor cells to modify
        case Some((x,y)) => {
          val newValue = randomDouble((steps(0) - deviation), (steps(0) + deviation))
          layer.setElementValue(x, y, newValue)
          constructionLayer.setToModified(x, y, "waterDepth")
          modifiedCells.append((x, y))
        }
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
          val currentDepth = layer.getElementValue(currentX, currentY).getOrElse(0.0)
          // Check if cell is on border
          for {
            x <- (currentX - 1) to (currentX + 1)
            y <- (currentY - 1) to (currentY + 1)
            if currentDepth < validThreshold
          } shouldErode = false
          // Erode
          if (shouldErode) {
            val newValue = currentDepth + randomDouble((step - deviation), (step + deviation))
            layer.setElementValue(currentX, currentY, newValue)
          }
        }
      }
      // Smooth modified area
      layer.smoothArea(modifiedCells, 3, 3)
    }
  }

}

object WaterPoolModificationForm {
  def formFields(): String = ""
}
