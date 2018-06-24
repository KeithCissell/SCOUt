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

// Elevation Modification
class ElevationModification(
  val name: String = "Elevation Modification",
  val elementType: String = "Elevation",
  val modification: Double,
  val deviation: Double,
  val coverage: Double,
  val slope: Double
) extends TerrainModification {
  // Constructor using mapped input from json decoder
  def this(formData: Map[String, String]) = this(
    modification = formData("Modification").toDouble,
    deviation = formData("Deviation").toDouble,
    coverage = formData("Coverage").toDouble,
    slope = formData("Slope").toDouble
  )

  // Shape mountains/hills/valleys (smooth along the way)
  def modify(layer: Layer, constructionLayer: ConstructionLayer) = constructionLayer.getRandomUnmodified() match {
    case None => // No unmodified cells
    case Some(startCell) => {
      // Set local variables
      var modifiedCells: AB[(Int,Int)] = AB()
      val numCellsToMod = Math.round(coverage * constructionLayer.cellCount).toInt
      // Initial modification
      val startX = startCell._1
      val startY = startCell._2
      layer.setElementValue(startX, startY, modification)
      constructionLayer.setToModified(startX, startY, "elevation")
      modifiedCells.append((startX, startY))
      // Move to random, unmodified neighbors and modify
      for (i <- 0 until numCellsToMod) constructionLayer.getNextUnmodifiedNeighbor(modifiedCells) match {
        case None => // No neighbor cells to modify
        case Some((x,y)) => {
          val currentValue = layer.getElementValue(x, y).getOrElse(0.0)
          val mod = randomDouble((modification - deviation), (modification + deviation))
          val newValue = currentValue + mod
          layer.setElementValue(x, y, newValue)
          constructionLayer.setToModified(x, y, "elevation")
          modifiedCells.append((x, y))
        }
      }
      // Apply sloping factor to modified area through smoothing
      val effectedRadius = Math.abs(Math.round(modification / slope).toInt)
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
        } layer.smooth(x, y, 2, dist(originX, originY, x, y))
      }
    }
  }

}

object ElevationModificationForm {
  def formFields(): String = """{
    "field-keys": [
      "Modification",
      "Deviation",
      "Coverage",
      "Slope"
    ],
    "fields": {
      "Modification": {
        "type": "number",
        "unit": "ft",
        "value": 0,
        "lowerBound": -1000,
        "upperBound": 1000
      },
      "Deviation": {
        "type": "number",
        "unit": "ft",
        "value": 5,
        "lowerBound": 0,
        "upperBound": 50
      },
      "Coverage": {
        "type": "number",
        "unit": "% of the environment",
        "value": 10,
        "lowerBound": 0,
        "upperBound": 100
      },
      "Slope": {
        "type": "number",
        "unit": "% of the modification area",
        "value": 70,
        "lowerBound": 0,
        "upperBound": 100
      }
    }
  }"""
}
