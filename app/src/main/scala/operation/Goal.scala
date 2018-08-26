package operation

import agent._
import environment._
import environment.cell._
import scoututil.Util._
import scala.collection.mutable.{Set => MutableSet}


trait Goal {
  val timeLimit: Option[Double] = None // in milliseconds
  def isComplete: Boolean = percentComplete >= 100.0
  def percentComplete: Double
  def update(environment: Environment, robot: Robot): Unit
}

// Find different anomalies in an environment
// anomaliesToFind is a map of (anomalyType -> # to find)
class FindAnomalies(anomaliesToFind: Map[String,Int], timeLimit: Option[Double]) extends Goal {
  // anomaliesFound keeps track of findings (anomalyType, x-position, y-position)
  var anomaliesFound: MutableSet[(String,Int,Int)] = MutableSet()

  def percentComplete: Double = {
    var numToFind = 0
    var numFound = 0
    for ((anomalyType, num) <- anomaliesToFind) {
      numToFind += num
      var typeFound = 0
      for (anomalyFound <- anomaliesFound) if (anomalyFound._1 == anomalyType) typeFound += 1
      numFound += Math.min(typeFound, num)
    }
    return (numFound.toDouble / numToFind.toDouble) * 100.0
  }

  def update(environment: Environment, robot: Robot): Unit = {
    val anomalyDetections = robot.detectAnomalies(environment)
    for (detection <- anomalyDetections) anomaliesFound += ((detection.anomalyType, detection.xFound, detection.yFound))
  }

}

// Map out as much of a certain element type as possible
class MapElements(mapHeight: Int, mapWidth: Int, elementsToMap: List[String], timeLimit: Option[Double]) extends Goal {
  // Copy of the Robot's internal map
  var internalMap: Grid[Cell] = emptyCellGrid(mapHeight, mapWidth)

  def percentComplete: Double = {
    var percentsTotal = 0.0
    for (elementType <- elementsToMap) percentsTotal += calculateTypeDiscovered(elementType)
    return percentsTotal / elementsToMap.length.toDouble
  }

  def calculateTypeDiscovered(elementType: String): Double = {
    var dataAvail = 0.0
    var dataKnown = 0.0
    for {
      x <- 0 until mapHeight
      y <- 0 until mapWidth
    } internalMap(x)(y) match {
      case None => dataAvail += 1.0
      case Some(cell) => {
        dataAvail += 1.0
        if (cell.containsElement(elementType)) dataKnown += 1.0
      }
    }
    return (dataKnown / dataAvail) * 100.0
  }

  def update(environment: Environment, robot: Robot): Unit = {
    internalMap = robot.internalMap
  }
}
