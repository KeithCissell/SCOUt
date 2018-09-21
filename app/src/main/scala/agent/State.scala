package scoutagent

import io.circe._
import io.circe.syntax._

import environment._
import environment.cell._
import environment.element._
import scoututil.Util._

import scala.collection.mutable.{ArrayBuffer => AB}
import scala.collection.mutable.{Map => MutableMap}


object State {
  // ----------------------------CLASSes----------------------------------------
  class AgentState(
    val health: Double,
    val energyLevel: Double,
    // val clock: Double,
    val elementStates: List[ElementState]
  ) {
    def elementTypes: List[String] = elementStates.map(_.elementType).toList
    def totalPercentKnown: Double = elementStates.map(_.percentKnown).foldLeft(0.0)(_ + _)
    def toJson(): Json = Json.obj(
      ("health", Json.fromDoubleOrNull(health)),
      ("energyLevel", Json.fromDoubleOrNull(energyLevel)),
      // ("clock", Json.fromDoubleOrNull(clock)),
      ("elementStates", Json.fromValues(elementStates.map(_.toJson())))
    )
    def getElementState(elementType: String): Option[ElementState] = {
      for (es <- elementStates) if (es.elementType == elementType) return Some(es)
      return None
    }
    def roundOff(fps: Int): AgentState = {
      val h = roundDoubleX(health, fps)
      val el = roundDoubleX(energyLevel, fps)
      val es = for (e <- elementStates) yield e.roundOff(fps)
      return new AgentState(h, el, es)
    }
  }

  class ElementState(
    val elementType: String,
    val indicator: Boolean,
    val hazard: Boolean,
    val percentKnownInRange: Double,
    val northQuadrant: QuadrantState,
    val southQuadrant: QuadrantState,
    val westQuadrant: QuadrantState,
    val eastQuadrant: QuadrantState
  ) {
    def percentKnown: Double = (northQuadrant.percentKnown + southQuadrant.percentKnown + westQuadrant.percentKnown + eastQuadrant.percentKnown) / 4
    def toJson(): Json = Json.obj(
      ("elementType", Json.fromString(elementType)),
      ("indicator", Json.fromBoolean(indicator)),
      ("hazard", Json.fromBoolean(hazard)),
      ("percentKnownInRange", Json.fromDoubleOrNull(percentKnownInRange)),
      ("north", northQuadrant.toJson()),
      ("south", southQuadrant.toJson()),
      ("west", westQuadrant.toJson()),
      ("east", eastQuadrant.toJson())
    )
    def getQuadrantState(q: String): QuadrantState = q match {
      case "north" => northQuadrant
      case "south" => southQuadrant
      case "west" => westQuadrant
      case "east" => eastQuadrant
    }
    def roundOff(fps: Int): ElementState = {
      val pkir = roundDoubleX(percentKnownInRange, fps)
      val nq = northQuadrant.roundOff(fps)
      val sq = southQuadrant.roundOff(fps)
      val wq = westQuadrant.roundOff(fps)
      val eq = eastQuadrant.roundOff(fps)
      return new ElementState(elementType, indicator, hazard, pkir, nq, sq, wq, eq)
    }
  }

  class QuadrantState(
    val percentKnown: Double,
    val averageValueDifferential: Option[Double],
    val immediateValueDifferential: Option[Double],
  ) {
    def toJson(): Json = Json.obj(
      ("percentKnown", Json.fromDoubleOrNull(percentKnown)),
      ("averageValueDifferential", Json.fromDoubleOrNull(averageValueDifferential.getOrElse(Double.NaN))),
      ("immediateValueDifferential", Json.fromDoubleOrNull(immediateValueDifferential.getOrElse(Double.NaN)))
    )
    def roundOff(fps: Int): QuadrantState = {
      val pk = roundDoubleX(percentKnown, fps)
      val avd = roundDoubleX(averageValueDifferential, fps)
      val ivd = roundDoubleX(immediateValueDifferential, fps)
      return new QuadrantState(pk, avd, ivd)
    }
  }

  // ----------------------------NORMALIZATION----------------------------------
  def normalizeAgentStatesGaussian(stateActionPairs: List[StateActionPair]): List[StateActionPair] = {
    val states: List[AgentState] = stateActionPairs.map(_.state)
    // Gather health data
    val healthValues: List[Double] = states.map(_.health)
    val healthGaussianData: GaussianData = calculateGaussianDistribution(healthValues)
    // Gather energyLevel data
    val energyLevelValues: List[Double] = states.map(_.energyLevel)
    val energyGaussianData: GaussianData = calculateGaussianDistribution(energyLevelValues)
    // Gather elementStates data
    val elementStates: List[ElementState] = states.flatMap(_.elementStates)
    var elementStateData: MutableMap[String,(AB[Double],AB[Double])] = MutableMap() // map(elementType -> (avgValDiff, immValDiff))
    for (es <- elementStates) elementStateData.get(es.elementType) match {
      case None => {
        val avds: AB[Double] = AB()
        val ivds: AB[Double] = AB()
        for (quad <- List(es.northQuadrant, es.southQuadrant, es.westQuadrant, es.eastQuadrant)) {
          if (quad.averageValueDifferential != None) avds += quad.averageValueDifferential.get
          if (quad.immediateValueDifferential != None) ivds += quad.immediateValueDifferential.get
        }
        elementStateData += (es.elementType -> (avds, ivds))
      }
      case Some((avds, ivds)) => {
        for (quad <- List(es.northQuadrant, es.southQuadrant, es.westQuadrant, es.eastQuadrant)) {
          if (quad.averageValueDifferential != None) avds += quad.averageValueDifferential.get
          if (quad.immediateValueDifferential != None) ivds += quad.immediateValueDifferential.get
        }
      }
    }
    val elementsGaussianData: Map[String,(GaussianData,GaussianData)] =
      (for ((eType, (avds, ivds)) <- elementStateData)
      yield (eType -> ((calculateGaussianDistribution(avds.toList), calculateGaussianDistribution(ivds.toList))))).toMap

    // Create normalized data
    val normalizedStateActionPairs = for (sap <- stateActionPairs) yield {
      val normalizedHealth = healthGaussianData.normalize(sap.state.health)
      val normalizedEnergy = energyGaussianData.normalize(sap.state.energyLevel)
      val normalizedElementStates = for (es <- sap.state.elementStates) yield {
        val avdsGaussianData = elementsGaussianData.get(es.elementType).get._1
        val ivdsGaussianData = elementsGaussianData.get(es.elementType).get._2
        val normalizedNQ = new QuadrantState(es.northQuadrant.percentKnown, avdsGaussianData.normalize(es.northQuadrant.averageValueDifferential), ivdsGaussianData.normalize(es.northQuadrant.immediateValueDifferential))
        val normalizedSQ = new QuadrantState(es.southQuadrant.percentKnown, avdsGaussianData.normalize(es.southQuadrant.averageValueDifferential), ivdsGaussianData.normalize(es.southQuadrant.immediateValueDifferential))
        val normalizedWQ = new QuadrantState(es.westQuadrant.percentKnown, avdsGaussianData.normalize(es.westQuadrant.averageValueDifferential), ivdsGaussianData.normalize(es.westQuadrant.immediateValueDifferential))
        val normalizedEQ = new QuadrantState(es.eastQuadrant.percentKnown, avdsGaussianData.normalize(es.eastQuadrant.averageValueDifferential), ivdsGaussianData.normalize(es.eastQuadrant.immediateValueDifferential))
        new ElementState(es.elementType, es.indicator, es.hazard, es.percentKnownInRange, normalizedNQ, normalizedSQ, normalizedWQ, normalizedEQ)
      }
      val normalizedState = new AgentState(normalizedHealth, normalizedEnergy, normalizedElementStates)
      new StateActionPair(normalizedState, sap.action, sap.shortTermScore, sap.longTermScore)
    }
    return normalizedStateActionPairs
  }

  class GaussianData(val mean: Double, val std: Double) {
    def normalize(x: Double): Double = (x - mean) / std
    def normalize(o: Option[Double]): Option[Double] = o match {
      case None => None
      case Some(x) => Some(normalize(x))
    }
  }

  def calculateGaussianDistribution(values: List[Double]): GaussianData = {
    val mean = values.foldLeft(0.0)(_ + _) / values.length
    val std = Math.sqrt(values.foldLeft(0.0)((a,b) => a + Math.pow((b - mean),2)) / values.length)
    return new GaussianData(mean, std)
  }

  // ----------------------------GENORATOR--------------------------------------
  def generateAgentState(agent: Agent): AgentState = {
    new AgentState(
      agent.health,
      agent.energyLevel,
      // agent.clock,
      getElementStates(agent))
  }

  def getElementStates(agent: Agent): List[ElementState] = {
    for (sensor <- agent.sensors) yield new ElementState(
      sensor.elementType,
      sensor.indicator,
      sensor.hazard,
      getPctCellsKnownInRange(agent, sensor.elementType, sensor.range),
      getQuadrantState(agent, "north", sensor.elementType),
      getQuadrantState(agent, "south", sensor.elementType),
      getQuadrantState(agent, "west", sensor.elementType),
      getQuadrantState(agent, "east", sensor.elementType))
  }

  def getPctCellsKnownInRange(agent: Agent, elementType: String, range: Double): Double = {
    val xPosition = agent.xPosition
    val yPosition = agent.yPosition
    val searchRadius = Math.max(range / agent.mapScale, 1.0)
    val cellBlockSize = Math.round(Math.abs(searchRadius)).toInt
    val cellsInRange = (for {
      x <- (xPosition - cellBlockSize) to (xPosition + cellBlockSize)
      y <- (yPosition - cellBlockSize) to (yPosition + cellBlockSize)
      if inMap(agent, x, y)
      if dist(x, y, xPosition, yPosition) <= searchRadius
    } yield agent.internalMap(x)(y).flatMap(_.get(elementType).flatMap(_.value))).toList
    return cellsInRange.flatten.length / cellsInRange.length
  }

  def getQuadrantState(agent: Agent, quadrant: String, elementType: String): QuadrantState = {
    val currentValue = agent.internalMap(agent.xPosition)(agent.yPosition).flatMap(_.get(elementType).flatMap(_.value))
    var cells: AB[Option[Cell]] = AB()
    var xImmediate = agent.xPosition
    var yImmediate = agent.yPosition
    quadrant match {
      case "north" => {
        cells = getNorthQuadrantCells(agent)
        xImmediate += 1 }
      case "south" => {
        cells = getSouthQuadrantCells(agent)
        xImmediate -= 1 }
      case "west" => {
        cells = getWestQuadrantCells(agent)
        yImmediate += 1 }
      case "east" => {
        cells = getEastQuadrantCells(agent)
        yImmediate -= 1 }
    }
    val elements: List[Element] = cells.flatMap(_.get.get(elementType)).toList
    val values: List[Double] = elements.map(_.value).flatten
    val pctKnown = if (cells.length > 0) values.length.toDouble / cells.length.toDouble else 1.0
    val averageValueDifferintial = currentValue match {
      case None => None
      case Some(curV) => (if (values.length > 0) Some(values.foldLeft(0.0)(_ + _) / values.length) else None) match {
        case None => None
        case Some(avgV) => Some(curV - avgV)
      }
    }
    val immediateValueDifferential = currentValue match {
      case None => None
      case Some(curV) => (if (inMap(agent, xImmediate, yImmediate)) agent.internalMap(xImmediate)(yImmediate).flatMap(_.get(elementType).flatMap(_.value)) else None) match {
        case None => None
        case Some(immV) => Some(curV - immV)
      }
    }
    return new QuadrantState(pctKnown, averageValueDifferintial, immediateValueDifferential)
  }

  def inMap(agent: Agent, x: Int, y: Int): Boolean = (x >= 0 && x < agent.mapHeight && y >= 0 && y < agent.mapWidth)

  def getNorthQuadrantCells(agent: Agent): AB[Option[Cell]] = {
    val xPosition = agent.xPosition
    val yPosition = agent.yPosition
    var cells: AB[Option[Cell]] = AB()
    for (x <- xPosition + 1 until agent.mapWidth) {
      for (y <- (yPosition - (x - xPosition)) to (yPosition + (x - xPosition))) {
        if (inMap(agent, x, y)) cells += agent.internalMap(x)(y)
      }
    }
    return cells
  }

  def getSouthQuadrantCells(agent: Agent): AB[Option[Cell]] = {
    val xPosition = agent.xPosition
    val yPosition = agent.yPosition
    var cells: AB[Option[Cell]] = AB()
    for (x <- 0 until xPosition) {
      for (y <- (yPosition - (xPosition - x)) to (yPosition + (xPosition - x))) {
        if (inMap(agent, x, y)) cells += agent.internalMap(x)(y)
      }
    }
    return cells
  }

  def getWestQuadrantCells(agent: Agent): AB[Option[Cell]] = {
    val xPosition = agent.xPosition
    val yPosition = agent.yPosition
    var cells: AB[Option[Cell]] = AB()
    for (y <- 0 until yPosition) {
      for (x <- (xPosition - (yPosition - y)) to (xPosition + (yPosition - y))) {
        if (inMap(agent, x, y)) cells += agent.internalMap(x)(y)
      }
    }
    return cells
  }

  def getEastQuadrantCells(agent: Agent): AB[Option[Cell]] = {
    val xPosition = agent.xPosition
    val yPosition = agent.yPosition
    var cells: AB[Option[Cell]] = AB()
    for (y <- yPosition + 1 until agent.mapHeight) {
      for (x <- (xPosition - (y - yPosition)) to (xPosition + (y - yPosition))) {
        if (inMap(agent, x, y)) cells += agent.internalMap(x)(y)
      }
    }
    return cells
  }
}
