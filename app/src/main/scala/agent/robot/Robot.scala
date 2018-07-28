package agent

import agent._
import environment._
import environment.cell._
import environment.element._
import scoututil.Util._

import scala.collection.mutable.{ArrayBuffer => AB}


object Robot {

  class Robot(
    val sensors: List[Sensor] = Nil,
    val mapHeight: Int = 0,
    val mapWidth: Int = 0,
    val movementCost: Double = 0.01,
    val slopeDamageThreshHold: Double = -3.0,
    val durability: Double = 1.0,
    var xPosition: Int = 0,
    var yPosition: Int = 0,
    var energyLevel: Double = 100.0,
    var health: Double = 100.0,
    var eventLog: AB[Event] = AB()
  ) {

    var clock: Double = 0.0 // in milliseconds
    var internalMap: Grid[Cell] = emptyCellGrid(mapHeight, mapWidth)

    def getState(): String = {
      s"""
      Position: ($xPosition, $yPosition)
      Energy: $energyLevel
      Helth: $health
      Clock: ${clock / 1000}
      Internal Map: ${calculateMapDiscovered()}% discovered
      """
    }

    def scan(env: Environment, elementType: String): Unit = {
      val startTime = System.currentTimeMillis()
      getSensor(elementType) match {
        case None => eventLog.append(new Event.SensorNotFound(s"Sensor for $elementType does not exist.", clock))
        case Some(sensor) => {
          if (energyLevel - sensor.energyExpense > 0) {
            // Update Internal Map
            val scanData = sensor.scan(env, xPosition, yPosition)
            for {
              x <- 0 until scanData.height
              y <- 0 until scanData.width
            } scanData.getElement(x, y) match {
              case None => // No element
              case Some(e: Element) => internalMap(x)(y) match {
                case None => // No cell
                case Some(cell) => cell.setElement(e)
              }
            }
            // Update energy level
            energyLevel -= sensor.energyExpense
            clock += sensor.runTime
            // Log event
            clock += System.currentTimeMillis() - startTime
            eventLog.append(new Event.Normal(s"Scanned for ${sensor.elementType}", clock))
          } else {
            clock += System.currentTimeMillis() - startTime
            eventLog.append(new Event.LowEnergy(s"Not enough energy to run scan for ${sensor.elementType}.", clock))
          }
        }
      }
    }

    def getSensor(elementType: String): Option[Sensor] = {
      for (sensor <- sensors) if (sensor.elementType == elementType) return Some(sensor)
      return None
    }

    def move(env: Environment, x: Int, y: Int): Unit = {
      val startTime = System.currentTimeMillis()
      // Calculate energy cost
      val x1 = xPosition * env.scale
      val y1 = yPosition * env.scale
      val z1 = env.getElementValue("Elevation", xPosition, yPosition).getOrElse(0.0)
      val x2 = x * env.scale
      val y2 = y * env.scale
      val z2 = env.getElementValue("Elevation", x, y).getOrElse(0.0)
      val slope = slope3D(x1, y1, z1, x2, y2, z2)
      val dist = dist3D(x1, y1, z1, x2, y2, z2)
      val cost = (1 + slope) * dist * movementCost
      if (energyLevel - cost > 0) {
        // Move
        xPosition = x
        yPosition = y
        energyLevel -= cost
        if (slope <= slopeDamageThreshHold) {
          // Calculate health reduction
          val healthReduction = Math.abs(slope) * 0.1 * (z2 - z1)
          health -= healthReduction
          if (health > 0) eventLog.append(new Event.TookDamage(s"Moved to ($x, $y). Took $healthReduction damage, health now at $health.", clock))
          else eventLog.append(new Event.Inoperational(s"Health droped below threshold. Robot inoperational.", clock))
        } else eventLog.append(new Event.Normal(s"Moved to ($x, $y)", clock))
      } else eventLog.append(new Event.LowEnergy(s"Not enough energy to move to ($x, $y)", clock))
    }

    def calculateMapDiscovered(): Double = {
      val discoveries = sensors.map(s => calculateTypeDiscovered(s.elementType)).toList
      if (discoveries.length > 0) return discoveries.sum / discoveries.length
      else return 100.0
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

  }

}
