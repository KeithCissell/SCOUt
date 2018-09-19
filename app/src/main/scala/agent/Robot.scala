package agent

import agent._
import agent.controller._
import environment._
import environment.cell._
import environment.element._
import environment.layer._
import scoututil.Util._

import scala.collection.mutable.{ArrayBuffer => AB}
import scala.collection.mutable.{Map => MutableMap}
import scala.collection.mutable.{Set => MutableSet}


class Robot(
  val name: String,
  val controller: Controller,
  val sensors: List[Sensor] = Nil,
  val mapHeight: Int = 0,
  val mapWidth: Int = 0,
  val mapScale: Double = 1,
  var xPosition: Int = 0,
  var yPosition: Int = 0
) {
  // Robot Satus Variables
  var internalMap: Grid[Cell] = emptyCellGrid(mapHeight, mapWidth)
  var health: Double = 100.0
  var energyLevel: Double = 100.0
  var clock: Double = 0.0 // in milliseconds

  // Universal damage and energy use variables
  var maxHealth: Double = 0.0
  var maxEnergyLevel: Double = 0.0
  val damageNormal: Double = 0.1
  val energyUseNormal: Double = 0.5
  val movementCost: Double = 0.01

  // Damage calculations
  val movementSlopeUpperThreshHold: Double = 1.0
  val movementSlopeLowerThreshHold: Double = -1.0
  val movementDamageResistance: Double = 0.0
  def calculateMovementDamage(slope: Double, dist: Double): Double = slope match {
    case slope if (slope > movementSlopeLowerThreshHold) => 0.0
    case _ => Math.abs(slope * dist * (2.0 - movementDamageResistance) * damageNormal)
  }
  def calculateMovementEnergyUse(slope: Double, dist: Double): Double = {
    (1.0 + slope) * dist * movementCost
  }
  def calculateMovementTime(slope: Double, dist: Double): Double = {
    (1.0 + slope) * dist * 1000.0
  }
  val temperatureDamageUpperThreshold: Double = 150.0
  val temperatureDamageLowerThreshold: Double = -50.0
  val temperatureDamageResistance: Double = 0.0
  def calculateTemperatureDamage(value: Double, threshHold: Double): Double = {
    Math.abs(value - threshHold) * (1.0 - temperatureDamageResistance) * damageNormal
  }
  val waterDepthDamageThreshHold: Double = 0.25
  val waterDepthFatalThreshHold: Double = 1.0
  val waterDepthDamageResistance: Double = 0.0
  def calculateWaterDepthDamage(value: Double, timeElapsed: Double): Double = (timeElapsed / 1000.0) * 5.0


  def operational = (health > 0.0 && energyLevel > 0.0)

  // AGENT STATE
  def getState(): AgentState = {
    new AgentState(
      health,
      energyLevel,
      // clock,
      getElementStates())
  }

  def getElementStates(): List[ElementState] = {
    for (sensor <- sensors) yield new ElementState(
      sensor.elementType,
      sensor.indicator,
      sensor.hazard,
      getPctCellsKnownInRange(sensor.elementType, sensor.range),
      internalMap(xPosition)(yPosition).flatMap(_.get(sensor.elementType).flatMap(_.value)),
      getQuadrantState("north", sensor.elementType),
      getQuadrantState("south", sensor.elementType),
      getQuadrantState("west", sensor.elementType),
      getQuadrantState("east", sensor.elementType))
  }

  def getPctCellsKnownInRange(elementType: String, range: Double): Double = {
    val searchRadius = Math.max(range / mapScale, 1.0)
    val cellBlockSize = Math.round(Math.abs(searchRadius)).toInt
    val cellsInRange = (for {
      x <- (xPosition - cellBlockSize) to (xPosition + cellBlockSize)
      y <- (yPosition - cellBlockSize) to (yPosition + cellBlockSize)
      if inMap(x, y)
      if dist(x, y, xPosition, yPosition) <= searchRadius
    } yield internalMap(x)(y).flatMap(_.get(elementType).flatMap(_.value))).toList
    return cellsInRange.flatten.length / cellsInRange.length
  }

  def getQuadrantState(quadrant: String, elementType: String): QuadrantState = {
    var cells: AB[Option[Cell]] = AB()
    var xImmediate = xPosition
    var yImmediate = yPosition
    quadrant match {
      case "north" => {
        cells = getNorthQuadrantCells()
        xImmediate += 1 }
      case "south" => {
        cells = getSouthQuadrantCells()
        xImmediate -= 1 }
      case "west" => {
        cells = getWestQuadrantCells()
        yImmediate += 1 }
      case "east" => {
        cells = getEastQuadrantCells()
        yImmediate -= 1 }
    }
    val elements: List[Element] = cells.flatMap(_.get.get(elementType)).toList
    val values: List[Double] = elements.map(_.value).flatten
    val pctKnown = if (cells.length > 0) values.length.toDouble / cells.length.toDouble else 1.0
    val avgVal = if (values.length > 0) Some(values.foldLeft(0.0)(_ + _) / values.length) else None
    val immediateVal = if (inMap(xImmediate, yImmediate)) internalMap(xImmediate)(yImmediate).flatMap(_.get(elementType).flatMap(_.value)) else None
    return new QuadrantState(pctKnown, avgVal, immediateVal)
  }

  def inMap(x: Int, y: Int): Boolean = (x >= 0 && x < mapHeight && y >= 0 && y < mapWidth)

  def getNorthQuadrantCells(): AB[Option[Cell]] = {
    var cells: AB[Option[Cell]] = AB()
    for (x <- xPosition + 1 until mapWidth) {
      for (y <- (yPosition - (x - xPosition)) to (yPosition + (x - xPosition))) {
        if (inMap(x,y)) cells += internalMap(x)(y)
      }
    }
    return cells
  }

  def getSouthQuadrantCells(): AB[Option[Cell]] = {
    var cells: AB[Option[Cell]] = AB()
    for (x <- 0 until xPosition) {
      for (y <- (yPosition - (xPosition - x)) to (yPosition + (xPosition - x))) {
        if (inMap(x,y)) cells += internalMap(x)(y)
      }
    }
    return cells
  }

  def getWestQuadrantCells(): AB[Option[Cell]] = {
    var cells: AB[Option[Cell]] = AB()
    for (y <- 0 until yPosition) {
      for (x <- (xPosition - (yPosition - y)) to (xPosition + (yPosition - y))) {
        if (inMap(x,y)) cells += internalMap(x)(y)
      }
    }
    return cells
  }

  def getEastQuadrantCells(): AB[Option[Cell]] = {
    var cells: AB[Option[Cell]] = AB()
    for (y <- yPosition + 1 until mapHeight) {
      for (x <- (xPosition - (y - yPosition)) to (xPosition + (y - yPosition))) {
        if (inMap(x,y)) cells += internalMap(x)(y)
      }
    }
    return cells
  }

  def statusString(): String = {
    s"""
    Position: ($xPosition, $yPosition)
    Energy: $energyLevel %
    Helth: $health %
    Clock: ${clock / 1000} s
    Internal Map: ${calculateMapDiscovered()} % discovered
    """
  }

  def setup: Unit = {
    maxHealth = health
    maxEnergyLevel = energyLevel
    controller.setup
  }

  def shutDown(stateActionPairs: List[StateActionPair]): Unit = {
    controller.shutDown(stateActionPairs)
  }

  def chooseAction(): String = controller.selectAction(getValidActions(), getState())

  def performAction(env: Environment, action: String): Event = action match {
    case "north"  => move(env, xPosition, yPosition + 1)
    case "south"  => move(env, xPosition, yPosition - 1)
    case "west"   => move(env, xPosition - 1, yPosition)
    case "east"   => move(env, xPosition + 1, yPosition)
    case elementType  => scan(env, elementType)
  }

  def getValidActions(): List[String] = {
    var validActions: MutableSet[String] = MutableSet()
    // Add valid movements
    if (yPosition < mapHeight - 1) validActions += "north"
    if (yPosition > 0) validActions += "south"
    if (xPosition > 0) validActions += "west"
    if (xPosition < mapWidth - 1) validActions += "east"
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
        val energyUse = sensor.energyExpense * energyUseNormal
        val timeElapsed = sensor.runTime
        val damage = calculateHazardDamage(env, xPosition, yPosition, timeElapsed)
        // Update status levels
        health = Math.max(health - damage, 0.0)
        energyLevel = Math.max(energyLevel - energyUse, 0.0)
        clock += timeElapsed
        // Return Event
        if (health <= 0.0) return new Event.HealthDepleted(s"Health droped below threshold. Robot inoperational.", clock, health, energyLevel, xPosition, yPosition)
        else if (energyLevel <= 0.0) return new Event.EnergyDepleted(s"Energy depleted atempting to scan for $elementType", clock, health, energyLevel, xPosition, yPosition)
        else return new Event.ScanSuccessful(s"Scanned for $elementType", clock, health, energyLevel, xPosition, yPosition, cellsScanned, newDiscoveries)
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
      case None => // No new data
      case Some(element: Element) => internalMap(x)(y) match {
        case None => {
          newDiscoveries += 1
          internalMap(x)(y) = Some(new Cell(x = x, y = y, elements = MutableMap(element.name -> element)))
        }
        case Some(cell) => {
          if (!cell.containsElement(element.name)) newDiscoveries += 1
          cell.setElement(element)
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
    // Calculate effects
    val energyUse = calculateMovementEnergyUse(slope, dist) * energyUseNormal
    val timeElapsed = calculateMovementTime(slope, dist)
    var movementDamage = calculateMovementDamage(slope, dist)
    // Check if movement is possible
    if (slope <= movementSlopeUpperThreshHold) {
      xPosition = x
      yPosition = y
    }
    // Apply hazard damage
    val hazardDamage = calculateHazardDamage(env, xPosition, yPosition, timeElapsed)
    val damage = hazardDamage + movementDamage
    // Adjust robot status levels
    health = Math.max(health - damage, 0.0)
    energyLevel = Math.max(energyLevel - energyUse, 0.0)
    clock += timeElapsed
    // Return Event
    if (health <= 0.0) return new Event.HealthDepleted(s"Health droped below threshold. Robot inoperational.", clock, health, energyLevel, xPosition, yPosition)
    else if (energyLevel <= 0.0) return new Event.EnergyDepleted(s"Energy depleted atempting to move to ($x, $y)", clock, health, energyLevel, xPosition, yPosition)
    else if (slope > movementSlopeUpperThreshHold) return new Event.MovementUnsuccessful(s"Cannot climb slope of $slope to move to ($x, $y).", clock, health, energyLevel, xPosition, yPosition)
    else {
      val msg = if (damage > 0.0) s"Moved to ($x, $y). Took $damage damage, health now at $health." else s"Moved to ($x, $y)"
      return new Event.MovementSuccessful(msg, clock, health, energyLevel, xPosition, yPosition)
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
          case _ => // No damage
        }
        case e: WaterDepth => e.value match {
          case Some(v) if (v > waterDepthFatalThreshHold) => damageTotal += 100.0
          case Some(v) if (v > waterDepthDamageThreshHold) => damageTotal += calculateWaterDepthDamage(v, timeElapsed)
          case _ => // No damage
        }
        case _ => // No damage
      }
      return damageTotal
    }
  }

  def detectAnomalies(env: Environment): AB[Event.AnomalyDetected] = {
    // Looks at current cell and 8 adjacent cells
    var events: AB[Event.AnomalyDetected] = AB()
    val cells = env.getClusterInclusive(xPosition, yPosition, 1.5)
    for {
      cell <- cells
      anomaly <- cell.anomalies
    } {
      internalMap(cell.x)(cell.y) match {
        case Some(mapCell) => mapCell.setAnomaly(anomaly)
        case None => internalMap(cell.x)(cell.y) = Some(new Cell(x = cell.x, y = cell.y, anomalies = MutableSet(anomaly)))
      }
      events += new Event.AnomalyDetected(s"$anomaly found at (${cell.x}, ${cell.y}).", clock, health, energyLevel, xPosition, yPosition, anomaly, cell.x, cell.y)
    }
    return events
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
