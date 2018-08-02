package agent

import agent._
import agent.controler._
import environment._
import environment.cell._
import environment.element._
import scoututil.Util._

import scala.collection.mutable.{ArrayBuffer => AB}
import scala.collection.mutable.{Set => MutableSet}


object Robot {

  class Robot(
    val controler: Controler,
    val sensors: List[Sensor] = Nil,
    val mapHeight: Int = 0,
    val mapWidth: Int = 0,
    var xPosition: Int = 0,
    var yPosition: Int = 0
  ) {
    // Robot behavior constraints
    val durability: Double = 1.0
    val movementCost: Double = 0.01
    val slopeClimbThreshHold: Double = 1.0
    val slopeFallThreshHold: Double = -3.0

    // Robot Satus Variables
    var internalMap: Grid[Cell] = emptyCellGrid(mapHeight, mapWidth)
    var energyLevel: Double = 100.0
    var health: Double = 100.0
    var clock: Double = 0.0 // in milliseconds
    var eventLog: AB[Event] = AB()

    def getState(): String = {
      s"""
      Position: ($xPosition, $yPosition)
      Energy: $energyLevel
      Helth: $health
      Clock: ${clock / 1000}
      Internal Map: ${calculateMapDiscovered()}% discovered
      """
    }

    def advanceState(env: Environment): Unit = {
      val validActions = getValidActions()
      val action = controler.selectAction(validActions, getState())
      val event = action match {
        case "up"     => move(env, xPosition + 1, yPosition)
        case "down"   => move(env, xPosition - 1, yPosition)
        case "left"   => move(env, xPosition, yPosition - 1)
        case "right"  => move(env, xPosition, yPosition + 1)
        case elementType  => scan(env, elementType)
      }
      eventLog.append(event)
    }

    def scan(env: Environment, elementType: String): Event = {
      val startTime = System.currentTimeMillis()
      getSensor(elementType) match {
        case None => return new Event.SensorNotFound(s"Sensor for $elementType does not exist.", clock)
        case Some(sensor) => {
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
          return new Event.Successful(s"Scanned for ${sensor.elementType}", clock)
        }
      }
    }

    def move(env: Environment, x: Int, y: Int): Event = {
      val startTime = System.currentTimeMillis()
      // Calculate movement slope and distance
      val x1: Double = xPosition.toDouble * env.scale
      val y1: Double = yPosition.toDouble * env.scale
      val z1: Double = env.getElementValue("Elevation", xPosition, yPosition).getOrElse(0.0)
      val x2: Double = x.toDouble * env.scale
      val y2: Double = y.toDouble * env.scale
      val z2: Double = env.getElementValue("Elevation", x, y).getOrElse(0.0)
      val slope = slope3D(x1, y1, z1, x2, y2, z2)
      val dist = dist3D(x1, y1, z1, x2, y2, z2)
      val zDist = z2 - z2
      // Account for hazards
      // ************************** TO-DO **************************
      val hazardDamage = 0.0
      // Calculate damage
      val movementDamage = calculateMovementDamage(slope, zDist)
      health = Math.max(health - movementDamage, 0.0)
      if (health <= 0.0) return new Event.HealthDepleted(s"Health droped below threshold. Robot inoperational.", clock)
      val damage = hazardDamage + movementDamage
      // Calculate movement cost
      val cost = calculateMovementCost(slope, dist)
      energyLevel = Math.max(energyLevel - cost, 0.0)
      if (energyLevel <= 0.0) return new Event.EnergyDepleted(s"Energy depleted atempting to move to ($x, $y)", clock)
      // Check if robot can climb slope
      if (slope > slopeClimbThreshHold) return new Event.CannotMove(s"Cannot climb slope to move to ($x, $y).", clock)
      // Move Robot
      xPosition = x
      yPosition = y
      if (damage > 0.0) return new Event.TookDamage(s"Moved to ($x, $y). Took $damage damage, health now at $health.", clock)
      else return new Event.Successful(s"Moved to ($x, $y)", clock)
    }

    def calculateMovementDamage(slope: Double, zDist: Double): Double = slope match {
      case s if (s > slopeFallThreshHold) => 0.0
      case _ => Math.abs(slope) * 0.1 * zDist
    }

    def calculateMovementCost(slope: Double, dist: Double): Double = {
      (1 + slope) * dist * movementCost
    }

    def getValidActions(): List[String] = {
      var validActions: MutableSet[String] = MutableSet()
      // Add valid movements
      if (xPosition != mapWidth) validActions += "up"
      if (xPosition != 0) validActions += "down"
      if (xPosition != 0) validActions += "left"
      if (xPosition != mapHeight) validActions += "right"
      // Add valid scan actions
      for (sensor <- sensors) if (energyLevel - sensor.energyExpense >= 0) validActions += sensor.elementType
      return validActions.toList
    }

    def getSensor(elementType: String): Option[Sensor] = {
      for (sensor <- sensors) if (sensor.elementType == elementType) return Some(sensor)
      return None
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
