package agent

import agent._
import agent.controler._
import environment._
import environment.cell._
import environment.element._
import environment.layer._
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
    // Robot Satus Variables
    var internalMap: Grid[Cell] = emptyCellGrid(mapHeight, mapWidth)
    var energyLevel: Double = 100.0
    var health: Double = 100.0
    var clock: Double = 0.0 // in milliseconds

    // Universal damage and energy use variables
    val damageNormal: Double = 0.1
    val energyUseNormal: Double = 0.1
    val movementCost: Double = 0.01

    // Damage calculations
    val movementUpperThreshHold: Double = 1.0
    val movementLowerThreshHold: Double = -3.0
    val movementDamageResistance: Double = 0.0
    def calculateMovementDamage(slope: Double, zDist: Double): Double = slope match {
      case s if (s > movementLowerThreshHold) => 0.0
      case _ => Math.abs(slope) * zDist * (1 + movementDamageResistance) * damageNormal
    }
    def calculateMovementCost(slope: Double, dist: Double): Double = {
      (1 + slope) * dist * movementCost
    }
    def calculateMovementTime(slope: Double, dist: Double): Double = {
      (1 + slope) * dist * 1000
    }
    val temperatureDamageUpperThreshold: Double = 150.0
    val temperatureDamageLowerThreshold: Double = -50.0
    val temperatureDamageResistance: Double = 0.0
    def calculateTemperatureDamage(value: Double, threshHold: Double): Double = {
      Math.abs(value - threshHold) * (1 + temperatureDamageResistance) * damageNormal
    }
    val waterDepthDamageThreshHold: Double = 0.25
    val waterDepthFatalThreshHold: Double = 1.0
    val waterDepthDamageResistance: Double = 0.0
    def calculateWaterDepthDamage(value: Double, timeElapsed: Double): Double = (timeElapsed / 1000) * 5.0


    def operational = (health > 0.0 && energyLevel > 0.0)

    def getState(): String = {
      s"""
      Position: ($xPosition, $yPosition)
      Energy: $energyLevel%
      Helth: $health%
      Clock: ${clock / 1000} s
      Internal Map: ${calculateMapDiscovered()}% discovered
      """
    }

    def advanceState(env: Environment): Unit = {
      val beginState = getState()
      val validActions = getValidActions()
      val action = controler.selectAction(validActions, beginState)
      val event = action match {
        case "up"     => move(env, xPosition + 1, yPosition)
        case "down"   => move(env, xPosition - 1, yPosition)
        case "left"   => move(env, xPosition, yPosition - 1)
        case "right"  => move(env, xPosition, yPosition + 1)
        case elementType  => scan(env, elementType)
      }
      controler.logEvent(beginState, action, event)
    }

    def getValidActions(): List[String] = {
      var validActions: MutableSet[String] = MutableSet()
      // Add valid movements
      if (xPosition < mapWidth) validActions += "up"
      if (xPosition > 0) validActions += "down"
      if (yPosition > 0) validActions += "left"
      if (yPosition < mapHeight) validActions += "right"
      // Add valid scan actions
      for (sensor <- sensors) if (energyLevel - sensor.energyExpense >= 0) validActions += sensor.elementType
      return validActions.toList
    }

    def scan(env: Environment, elementType: String): Event = {
      getSensor(elementType) match {
        case None => return new Event.ScanUnsuccessful(s"Sensor for $elementType does not exist.", clock, health, energyLevel, xPosition, yPosition)
        case Some(sensor) => {
          // Update Internal Map
          val scanData = sensor.scan(env, xPosition, yPosition)
          val cellsScanned = sensor.cellRange(env, xPosition, yPosition)
          val newDiscoveries = addScanData(scanData)
          // Account for hazard damage
          val energyUse = sensor.energyExpense
          val timeElapsed = sensor.runTime
          val damage = calculateHazardDamage(env, xPosition, yPosition, timeElapsed)
          // Update status levels
          health = Math.max(health - damage, 0.0)
          energyLevel = Math.max(energyLevel - energyUse, 0.0)
          clock += timeElapsed
          // Return Event
          if (health <= 0.0) return new Event.HealthDepleted(s"Health droped below threshold. Robot inoperational.", clock, health, energyLevel, xPosition, yPosition)
          else if (energyLevel <= 0.0) return new Event.EnergyDepleted(s"Energy depleted atempting to scan for $elementType", clock, health, energyLevel, xPosition, yPosition)
          else return new Event.ScanSuccessful(s"Scanned for $elementType", clock, health, energyLevel, xPosition, yPosition, energyUse, cellsScanned, newDiscoveries)
        }
      }
    }

    def getSensor(elementType: String): Option[Sensor] = {
      for (sensor <- sensors) if (sensor.elementType == elementType) return Some(sensor)
      return None
    }

    def addScanData(scanData: Layer): Int = {
      var newDiscoveries = 0
      for {
        x <- 0 until scanData.height
        y <- 0 until scanData.width
      } scanData.getElement(x, y) match {
        case None => // No element
        case Some(e: Element) => internalMap(x)(y) match {
          case None => // No cell
          case Some(cell) => {
            cell.setElement(e)
            newDiscoveries += 1
          }
        }
      }
      return newDiscoveries
    }

    def move(env: Environment, x: Int, y: Int): Event = {
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
      // Calculate effects
      val cost = calculateMovementCost(slope, dist)
      val timeElapsed = calculateMovementTime(slope, dist)
      val movementDamage = calculateMovementDamage(slope, zDist)
      val hazardDamage = calculateHazardDamage(env, x, y, timeElapsed)
      val damage = hazardDamage + movementDamage
      // Adjust robot status levels
      health = Math.max(health - damage, 0.0)
      energyLevel = Math.max(energyLevel - cost, 0.0)
      clock += timeElapsed
      // Return Event
      if (health <= 0.0) return new Event.HealthDepleted(s"Health droped below threshold. Robot inoperational.", clock, health, energyLevel, xPosition, yPosition)
      else if (energyLevel <= 0.0) return new Event.EnergyDepleted(s"Energy depleted atempting to move to ($x, $y)", clock, health, energyLevel, xPosition, yPosition)
      else if (slope > movementUpperThreshHold) return new Event.MovementUnsuccessful(s"Cannot climb slope of $slope to move to ($x, $y).", clock, health, energyLevel, xPosition, yPosition, cost, movementDamage)
      else {
        xPosition = x
        yPosition = y
        val msg = if (damage > 0.0) s"Moved to ($x, $y). Took $damage damage, health now at $health." else s"Moved to ($x, $y)"
        return new Event.MovementSuccessful(msg, clock, health, energyLevel, xPosition, yPosition, cost, damage)
      }
    }

    def calculateHazardDamage(env: Environment, x: Int, y: Int, timeElapsed: Double): Double = env.getCell(x, y) match {
      case None => 0.0
      case Some(cell) => {
        var damageTotal = 0.0
        for (element <- cell.elements.values) element match {
          case e: Temperature => e.value match {
            case Some(v) if (v > temperatureDamageUpperThreshold) => damageTotal += calculateTemperatureDamage(v, temperatureDamageUpperThreshold)
            case Some(v) if (v < temperatureDamageLowerThreshold) => damageTotal += calculateTemperatureDamage(v, temperatureDamageLowerThreshold)
          }
          case e: WaterDepth => e.value match {
            case Some(v) if (v > waterDepthFatalThreshHold) => damageTotal += 100.0
            case Some(v) if (v > waterDepthDamageThreshHold) => damageTotal += calculateWaterDepthDamage(v, timeElapsed)
          }
        }
        return damageTotal
      }
    }

    def detectAnomaly(env: Environment): Event = {
      // Looks at current cell and 8 adjacent cells
      val anomalies: List[String] = env.getCluster(xPosition, yPosition, 1.5).map(_.anomalies.toList).flatten
      if (anomalies.length > 0) return new Event.AnomalyDetectionSuccessful(s"Anomalies found.", clock, health, energyLevel, xPosition, yPosition, anomalies)
      else return new Event.AnomalyDetectionUnsuccessful(s"No anomalies found.", clock, health, energyLevel, xPosition, yPosition)
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
