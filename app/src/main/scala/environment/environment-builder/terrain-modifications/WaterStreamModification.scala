package environment.modifications

import environment._
import environment.cell._
import environment.element._
import environment.element.seed._
import environment.layer._
import environment.modifications._

import scoututil.Util._

import scala.util.Random
import scala.collection.mutable.{ArrayBuffer => AB}


// Water Stream Modification
case class WaterStreamModification(
  val elementType: String = "Water Depth",
  val depth: Double,
  val deviation: Double,
  val width: Double,
  val length: Double
) extends TerrainModification {
  // Erode channels of water with a directional influence
  def modify(layer: Layer, constructionLayer: ConstructionLayer) = constructionLayer.getRandomUnmodified() match {
    case None => // No unmodified cells
    case Some(startCell) => {
      // Set local variables
      var modifiedCells: AB[(Int,Int)] = AB()
      val modCellLength = Math.ceil(length / constructionLayer.scale).toInt
      val modCellWidth = Math.ceil(width / constructionLayer.scale).toInt - 1
      // Initialize starting point of stream
      var currentX = startCell._1
      var currentY = startCell._2
      layer.setElementValue(currentX, currentY, depth)
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
          val newValue = depth + randomDouble((depth - deviation), (depth + deviation))
          layer.setElementValue(currentX, currentY, newValue)
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
            val newValue = depth + randomDouble((depth - deviation), (depth + deviation))
            layer.setElementValue(currentX, currentY, newValue)
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
            val newValue = depth + randomDouble((depth - deviation), (depth + deviation))
            layer.setElementValue(currentX, currentY, newValue)
            constructionLayer.setToModified(currentX, currentY, "waterDepth")
            modifiedCells.append((currentX, currentY))
            i += 1
          }
          case None => done = true
        }
      }
      // Smooth the stream
      layer.smoothAreaNeighbors(modifiedCells, 3, 3)
    }
  }

}
