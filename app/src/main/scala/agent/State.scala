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
    val xPosition: Int,
    val yPosition: Int,
    val health: Double,
    val energyLevel: Double,
    val elementStates: List[ElementState]
  ) {
    def elementTypes: List[String] = elementStates.map(_.elementType).toList
    def totalPercentKnown: Double = elementStates.map(_.percentKnown).foldLeft(0.0)(_ + _)
    def toJson(): Json = Json.obj(
      ("xPosition", Json.fromInt(xPosition)),
      ("yPosition", Json.fromInt(yPosition)),
      ("health", Json.fromDoubleOrNull(health)),
      ("energyLevel", Json.fromDoubleOrNull(energyLevel)),
      ("elementStates", Json.fromValues(elementStates.map(_.toJson())))
    )
    def toJsonIndexed(): Json = Json.fromValues(List(
      Json.fromInt(xPosition),
      Json.fromInt(yPosition),
      Json.fromDoubleOrNull(health),
      Json.fromDoubleOrNull(energyLevel),
      Json.fromValues(elementStates.map(_.toJsonIndexed()))
    ))
    def getElementState(elementType: String): Option[ElementState] = {
      for (es <- elementStates) if (es.elementType == elementType) return Some(es)
      return None
    }
    def roundOff(fps: Int): AgentState = {
      val h = roundDoubleX(health, fps)
      val el = roundDoubleX(energyLevel, fps)
      val es = for (e <- elementStates) yield e.roundOff(fps)
      return new AgentState(xPosition, yPosition, h, el, es)
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
    def immediateValuesKnown: Int = {
      val immVals: List[Option[Double]] = List(northQuadrant.immediateValueDifferential, southQuadrant.immediateValueDifferential, westQuadrant.immediateValueDifferential, eastQuadrant.immediateValueDifferential)
      val knownImmVals: List[Double] = immVals.flatten
      return knownImmVals.length
    }
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
    def toJsonIndexed(): Json = Json.fromValues(List(
      Json.fromString(elementType),
      Json.fromBoolean(indicator),
      Json.fromBoolean(hazard),
      Json.fromDoubleOrNull(percentKnownInRange),
      northQuadrant.toJsonIndexed(),
      southQuadrant.toJsonIndexed(),
      westQuadrant.toJsonIndexed(),
      eastQuadrant.toJsonIndexed()
    ))
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
    val immediateValueDifferential: Option[Double]
  ) {
    def toJson(): Json = Json.obj(
      ("percentKnown", Json.fromDoubleOrNull(percentKnown)),
      ("averageValueDifferential", Json.fromDoubleOrNull(averageValueDifferential.getOrElse(Double.NaN))),
      ("immediateValueDifferential", Json.fromDoubleOrNull(immediateValueDifferential.getOrElse(Double.NaN)))
    )
    def toJsonIndexed(): Json = Json.fromValues(List(
      Json.fromDoubleOrNull(percentKnown),
      Json.fromDoubleOrNull(averageValueDifferential.getOrElse(Double.NaN)),
      Json.fromDoubleOrNull(immediateValueDifferential.getOrElse(Double.NaN))
    ))
    def roundOff(fps: Int): QuadrantState = {
      val pk = roundDoubleX(percentKnown, fps)
      val avd = roundDoubleX(averageValueDifferential, fps)
      val ivd = roundDoubleX(immediateValueDifferential, fps)
      return new QuadrantState(pk, avd, ivd)
    }
  }

  class StateActionPair(
    val state: AgentState,
    val action: String,
    val shortTermScore: Double,
    val longTermScore: Double
  ) {

    def toJson(): Json = Json.obj(
      ("state", state.toJson()),
      ("action", Json.fromString(action)),
      ("shortTermScore", Json.fromDoubleOrNull(shortTermScore)),
      ("longTermScore", Json.fromDoubleOrNull(longTermScore))
    )

    def toJsonIndexed(): Json = Json.fromValues(List(
      state.toJsonIndexed(),
      Json.fromString(action),
      Json.fromDoubleOrNull(shortTermScore),
      Json.fromDoubleOrNull(longTermScore)
    ))


    def roundOff(fps: Int): StateActionPair = {
      val sts = roundDoubleX(shortTermScore, fps)
      val lts = roundDoubleX(longTermScore, fps)
      return new StateActionPair(state.roundOff(fps), action, sts, lts)
    }

  }

  // --------------------ACTION ANALYSIS-----------------------------
  def isMovementAction(action: String): Boolean = Set("north","south","west","east").contains(action)
  def getOppositeOrientation(orientation: String): String = orientation match {
    case "north" => "south"
    case "south" => "north"
    case "west" => "east"
    case "east" => "west"
  }
  def getClockwiseOrientation(orientation: String): String = orientation match {
    case "north" => "east"
    case "south" => "west"
    case "west" => "north"
    case "east" => "south"
  }
  def getCounterClockwiseOrientation(orientation: String): String = orientation match {
    case "north" => "west"
    case "south" => "east"
    case "west" => "south"
    case "east" => "north"
  }

  // ----------------------------GENORATOR--------------------------------------
  def generateAgentState(agent: Agent): AgentState = {
    new AgentState(
      agent.xPosition,
      agent.yPosition,
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


  // ----------------------------NORMALIZATION----------------------------------
  class NormalizedStateActionPairs(stateActionPairs: List[StateActionPair]) {
    // CALCULATE NORMALS
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
    val normalizedStateActionPairs: List[StateActionPair] = for (sap <- stateActionPairs) yield {
      val normalizedState = normalizeState(sap.state)
      new StateActionPair(normalizedState, sap.action, sap.shortTermScore, sap.longTermScore)
    }

    // Normalize Given State
    def normalizeState(state: AgentState): AgentState = {
      val normalizedHealth = healthGaussianData.normalize(state.health)
      val normalizedEnergy = energyGaussianData.normalize(state.energyLevel)
      val normalizedElementStates = for (es <- state.elementStates) yield {
        elementsGaussianData.get(es.elementType) match {
          case Some(gd) => {
            val avdsGaussianData = gd._1
            val ivdsGaussianData = gd._2
            val normalizedNQ = new QuadrantState(es.northQuadrant.percentKnown, avdsGaussianData.normalize(es.northQuadrant.averageValueDifferential), ivdsGaussianData.normalize(es.northQuadrant.immediateValueDifferential))
            val normalizedSQ = new QuadrantState(es.southQuadrant.percentKnown, avdsGaussianData.normalize(es.southQuadrant.averageValueDifferential), ivdsGaussianData.normalize(es.southQuadrant.immediateValueDifferential))
            val normalizedWQ = new QuadrantState(es.westQuadrant.percentKnown, avdsGaussianData.normalize(es.westQuadrant.averageValueDifferential), ivdsGaussianData.normalize(es.westQuadrant.immediateValueDifferential))
            val normalizedEQ = new QuadrantState(es.eastQuadrant.percentKnown, avdsGaussianData.normalize(es.eastQuadrant.averageValueDifferential), ivdsGaussianData.normalize(es.eastQuadrant.immediateValueDifferential))
            new ElementState(es.elementType, es.indicator, es.hazard, es.percentKnownInRange, normalizedNQ, normalizedSQ, normalizedWQ, normalizedEQ)
          }
          case None => es
        }
      }
      new AgentState(state.xPosition, state.yPosition, normalizedHealth, normalizedEnergy, normalizedElementStates)
    }


    // DIFFERENCES
    def calculateStateActionDifferences(rawState: AgentState, weights: StateDifferenceWeights): List[StateActionDifference] = {
      // Normalize Raw State
      val state = normalizeState(rawState)
      // Generate list
      val differences: List[StateActionDifference] = for (sap <- normalizedStateActionPairs) yield {
        // Extract State Action Pair Data
        val cState: AgentState = sap.state
        val cSTS: Double = sap.shortTermScore
        val cLTS: Double = sap.longTermScore
        // Calculate Differences
        val healthDifference: Double = Math.abs(state.health - cState.health)
        val energyDifference: Double = Math.abs(state.energyLevel - cState.energyLevel)
        val elementStateDifferences: List[ElementStateDifference] = calculateElementStateDifferences(state, cState)
        val northComparisons: QuadrantComparisons = calculateQuadrantComparisons(state, "north", cState)
        val southComparisons: QuadrantComparisons = calculateQuadrantComparisons(state, "south", cState)
        val westComparisons: QuadrantComparisons = calculateQuadrantComparisons(state, "west", cState)
        val eastComparisons: QuadrantComparisons = calculateQuadrantComparisons(state, "east", cState)
        val stateDifference = new StateDifference(healthDifference, energyDifference, elementStateDifferences, northComparisons, southComparisons, westComparisons, eastComparisons)
        // Calculate Overall Difference
        val overallDifference = stateDifference.overallDifference(sap.action, weights)
        // If movement: Orient Movement Direction relative to current state
        val cAction = if (isMovementAction(sap.action)) overallDifference._2 match {
          case "north" => sap.action
          case "south" => getOppositeOrientation(sap.action)
          case "west" => getClockwiseOrientation(sap.action)
          case "east" => getCounterClockwiseOrientation(sap.action)
        } else sap.action
        new StateActionDifference(stateDifference, cAction, overallDifference._1, cSTS, cLTS)
      }
      return differences
    }

    // ELEMENT STATE DIFFERENCES
    def calculateElementStateDifferences(state: AgentState, cState: AgentState): List[ElementStateDifference] = for (es <- state.elementStates) yield {
      val elementType = es.elementType
      cState.getElementState(elementType) match {
        case None => new ElementStateDifference(elementType, es.indicator, es.hazard, 1.0, 1.0)
        case Some(ces) => {
          val percentKnownInRangeDifference: Double = Math.abs(es.percentKnownInRange - ces.percentKnownInRange)
          val immediateKnownDifference: Double = Math.abs(es.immediateValuesKnown - ces.immediateValuesKnown) / 4.0
          new ElementStateDifference(elementType, es.indicator, es.hazard, percentKnownInRangeDifference, immediateKnownDifference)
        }
      }
    }

    // QUADRANT DIFFERENCE
    def calculateQuadrantComparisons(state: AgentState, orientation: String, cState: AgentState): QuadrantComparisons = {
      val northQuadrantDifference: QuadrantDifference = calculateQuadrantDifference(state, orientation, cState, "north")
      val southQuadrantDifference: QuadrantDifference = calculateQuadrantDifference(state, orientation, cState, "south")
      val westQuadrantDifference: QuadrantDifference = calculateQuadrantDifference(state, orientation, cState, "west")
      val eastQuadrantDifference: QuadrantDifference = calculateQuadrantDifference(state, orientation, cState, "east")
      new QuadrantComparisons(northQuadrantDifference, southQuadrantDifference, westQuadrantDifference, eastQuadrantDifference)
    }

    def calculateQuadrantDifference(state: AgentState, orientation: String, cState: AgentState, cOrientation: String): QuadrantDifference = {
      val quadrantElementStateDifferences = for (es <- state.elementStates) yield {
        val elementType = es.elementType
        cState.getElementState(elementType) match {
          case None => new QuadrantElementStateDifference(elementType, es.indicator, es.hazard, 1.0, 1.0, 1.0)
          case Some(ces) => CalculateQuadrantElementStateDifference(es, orientation, ces, cOrientation)
        }
      }
      return new QuadrantDifference(quadrantElementStateDifferences)
    }

    def CalculateQuadrantElementStateDifference(es: ElementState, orientation: String, ces: ElementState, cOrientation: String): QuadrantElementStateDifference = {
      val quadrant = es.getQuadrantState(orientation)
      val cQuadrant = ces.getQuadrantState(cOrientation)
      val percentKnownDifference: Double = Math.abs(quadrant.percentKnown - cQuadrant.percentKnown)
      val averageValueDifference: Double = (quadrant.averageValueDifferential, cQuadrant.averageValueDifferential) match {
        case (Some(avd), Some(cavd)) => Math.abs(avd - cavd)
        case (None, None) => 0.0
        case _ => 1.0
      }
      val immediateValueDifference: Double = (quadrant.immediateValueDifferential, cQuadrant.immediateValueDifferential) match {
        case (Some(ivd), Some(civd)) => Math.abs(ivd - civd)
        case (None, None) => 0.0
        case _ => 1.0
      }
      new QuadrantElementStateDifference(es.elementType, es.indicator, es.hazard, percentKnownDifference, averageValueDifference, immediateValueDifference)
    }

  }

  class GaussianData(val mean: Double, val std: Double) {
    def normalize(x: Double): Double = if (std != 0.0) (x - mean) / std else 0.0
    def normalize(o: Option[Double]): Option[Double] = o match {
      case None => None
      case Some(x) if (x.isNaN) => None
      case Some(x) => Some(normalize(x))
    }
  }

  // Find the Gaussian Distribution of data values
  def calculateGaussianDistribution(values: List[Double]): GaussianData = {
    if (values.length > 0) {
      val mean = values.foldLeft(0.0)(_ + _) / values.length
      val std = Math.sqrt(values.foldLeft(0.0)((a,b) => a + Math.pow((b - mean),2)) / values.length)
      return new GaussianData(mean, std)
    } else return new GaussianData(0.0, 0.0)
  }

  // DIFFERENCES
  class StateActionDifference(
    val stateDifference: StateDifference,
    val action: String,
    val overallDifference: Double,
    val shortTermScore: Double,
    val longTermScore: Double
  ) {
    def print = {
      println()
      println(s"      Action: $action")
      println(s"      OverallDiff: $overallDifference")
      println(s"      STS: $shortTermScore")
      println(s"      LTS: $longTermScore")
      println()
    }
  }

  class StateDifference(
    val healthDifference: Double,
    val energyDifference: Double,
    val elementStateDifferences: List[ElementStateDifference],
    val northComparisons: QuadrantComparisons,
    val southComparisons: QuadrantComparisons,
    val westComparisons: QuadrantComparisons,
    val eastComparisons: QuadrantComparisons
  ) {
    def overallDifference(action: String, weights: StateDifferenceWeights): (Double,String) = {
      if (isMovementAction(action)) movementDifference(weights)
      else scanDifference(weights)
    }

    def movementDifference(weights: StateDifferenceWeights): (Double,String) = {
      val h = healthDifference * weights.healthWeight
      val e = energyDifference * weights.energyWeight
      val es = averageElementStateDifference(weights.elementDifferenceWeights) * weights.elementStateWeight
      val lqd = lowestQuadrantsDifference(weights.quadrantDifferenceWeights)
      val qd = lqd._1 * weights.totalQuadrantWeight
      val overallDifference = (h + e + qd) / weights.movementTotal
      val orientation = lqd._2
      return (overallDifference, orientation)
    }
    def scanDifference(weights: StateDifferenceWeights): (Double,String) = {
      val h = healthDifference * weights.healthWeight
      val e = energyDifference * weights.energyWeight
      val es = averageElementStateDifference(weights.elementDifferenceWeights) * weights.elementStateWeight
      val overallDifference = (h + e + es) / weights.scanTotal
      return (overallDifference, "")
    }
    def averageElementStateDifference(weights: ElementDifferenceWeights): Double = {
      val diffs = for (esd <- elementStateDifferences) yield esd.overallDifference(weights)
      return if (diffs.length > 0) diffs.foldLeft(0.0)(_ + _) / diffs.length else 1.0
    }
    def lowestQuadrantsDifference(weights: QuadrantDifferenceWeights): (Double,String) = {
      val northToNorth = {
        val n = northComparisons.overallDifference(weights, "north")
        val e = eastComparisons.overallDifference(weights, "east")
        val s = southComparisons.overallDifference(weights, "south")
        val w = westComparisons.overallDifference(weights, "west")
        ((n + e + s + w) / 4.0, "north")
      }
      val northToEast = {
        val n = northComparisons.overallDifference(weights, "east")
        val e = eastComparisons.overallDifference(weights, "south")
        val s = southComparisons.overallDifference(weights, "west")
        val w = westComparisons.overallDifference(weights, "north")
        ((n + e + s + w) / 4.0, "east")
      }
      val northToSouth = {
        val n = northComparisons.overallDifference(weights, "south")
        val e = eastComparisons.overallDifference(weights, "west")
        val s = southComparisons.overallDifference(weights, "north")
        val w = westComparisons.overallDifference(weights, "east")
        ((n + e + s + w) / 4.0, "south")
      }
      val northToWest = {
        val n = northComparisons.overallDifference(weights, "west")
        val e = eastComparisons.overallDifference(weights, "north")
        val s = southComparisons.overallDifference(weights, "east")
        val w = westComparisons.overallDifference(weights, "south")
        ((n + e + s + w) / 4.0, "west")
      }
      // Find Orientation with lowest differnce
      var lowestDifferenceOrientation = northToNorth
      for ((s,o) <- List(northToEast, northToSouth, northToWest)) {
        if (s < lowestDifferenceOrientation._1) lowestDifferenceOrientation = (s,o)
      }
      return lowestDifferenceOrientation
    }
  }

  class ElementStateDifference(
    val elementType: String,
    val indicator: Boolean,
    val hazard: Boolean,
    val percentKnownInRangeDifference: Double,
    val immediateKnownDifference: Double
  ) {
    def overallDifference(weights: ElementDifferenceWeights): Double = {
      val pkir = percentKnownInRangeDifference * weights.percentKnownInRangeWeight
      val ik = immediateKnownDifference * weights.immediateKnownWeight
      var overallDifference = (pkir + ik) / weights.total
      if (indicator) overallDifference *= weights.indicatorWeight
      if (hazard) overallDifference *= weights.hazardWeight
      return overallDifference / weights.elementTypeTotal
    }
  }

  class QuadrantComparisons(
    val northQuadrantDifference: QuadrantDifference,
    val southQuadrantDifference: QuadrantDifference,
    val westQuadrantDifference: QuadrantDifference,
    val eastQuadrantDifference: QuadrantDifference
  ) {
    def overallDifference(weights: QuadrantDifferenceWeights, orientation: String): Double = orientation match {
      case "north" => northQuadrantDifference.overallDifference(weights)
      case "south" => southQuadrantDifference.overallDifference(weights)
      case "west" => westQuadrantDifference.overallDifference(weights)
      case "east" => eastQuadrantDifference.overallDifference(weights)
    }
  }

  class QuadrantDifference(
    val quadrantElementStateDifferences: List[QuadrantElementStateDifference]
  ) {
    def overallDifference(weights: QuadrantDifferenceWeights): Double = {
      val diffs = for (qesd <- quadrantElementStateDifferences) yield qesd.overallDifference(weights)
      return if (diffs.length > 0) diffs.foldLeft(0.0)(_ + _) / diffs.length else 1.0
    }
  }

  class QuadrantElementStateDifference(
    val elementType: String,
    val indicator: Boolean,
    val hazard: Boolean,
    val percentKnownDifference: Double,
    val averageValueDifference: Double,
    val immediateValueDifference: Double
  ) {
    def overallDifference(weights: QuadrantDifferenceWeights): Double = {
      val pk = percentKnownDifference * weights.percentKnownWeight
      val av = averageValueDifference * weights.averageValueWeight
      val iv = immediateValueDifference * weights.immediateValueWeight
      var overallDifference = (pk + av + iv) / weights.total
      if (indicator) overallDifference *= weights.indicatorWeight
      if (hazard) overallDifference *= weights.hazardWeight
      return overallDifference / weights.elementTypeTotal
    }
  }

  // WEIGHTS
  class StateDifferenceWeights(
    val healthWeight: Double,
    val energyWeight: Double,
    val elementStateWeight: Double,
    val totalQuadrantWeight: Double,
    val elementDifferenceWeights: ElementDifferenceWeights,
    val quadrantDifferenceWeights: QuadrantDifferenceWeights
  ) {
    def total: Double = healthWeight + energyWeight + elementStateWeight + totalQuadrantWeight
    def movementTotal: Double = healthWeight + energyWeight + totalQuadrantWeight
    def scanTotal: Double = healthWeight + energyWeight + elementStateWeight
  }

  class ElementDifferenceWeights(
    val indicatorWeight: Double,
    val hazardWeight: Double,
    val percentKnownInRangeWeight: Double,
    val immediateKnownWeight: Double
  ) {
    def total: Double = percentKnownInRangeWeight + immediateKnownWeight
    def elementTypeTotal: Double = indicatorWeight + hazardWeight
  }

  class QuadrantDifferenceWeights(
    val indicatorWeight: Double,
    val hazardWeight: Double,
    val percentKnownWeight: Double,
    val averageValueWeight: Double,
    val immediateValueWeight: Double
  ) {
    def total: Double = percentKnownWeight + averageValueWeight + immediateValueWeight
    def elementTypeTotal: Double = indicatorWeight + hazardWeight
  }

}
