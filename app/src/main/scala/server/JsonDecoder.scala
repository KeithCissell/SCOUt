package jsonhandler

import io.circe._
import scoutagent._
import scoutagent.State._
import environment._
import environment.cell._
import environment.element._
import environment.element.seed._
import environment.anomaly._
import environment.terrainmodification._
import scala.collection.mutable
import scoututil.Util._

import scala.collection.mutable.{Map => MutableMap}
import scala.collection.mutable.{Set => MutableSet}
import scala.collection.mutable.{ArrayBuffer => AB}


object Decoder {

  // ------------------- GENERAL EXTRACTORS ------------------------------------

  def extractString(field: String, data: Json): Option[String] = {
    val cursor: HCursor = data.hcursor
    cursor.downField(field).as[String] match {
      case Left(_)  => None
      case Right(s) => Some(s)
    }
  }

  def extractString(field: String, data: ACursor): Option[String] = {
    data.downField(field).as[String] match {
      case Left(_)  => None
      case Right(s) => Some(s)
    }
  }

  def extractInt(field: String, data: Json): Option[Int] = {
    val cursor: HCursor = data.hcursor
    cursor.downField(field).as[Int] match {
      case Left(_)  => None
      case Right(i) => Some(i)
    }
  }

  def extractInt(field: String, data: ACursor): Option[Int] = {
    data.downField(field).as[Int] match {
      case Left(_)  => None
      case Right(i) => Some(i)
    }
  }

  def extractDouble(field: String, data: Json): Option[Double] = {
    val cursor: HCursor = data.hcursor
    cursor.downField(field).as[Double] match {
      case Left(_)  => None
      case Right(d) => d match {
        case n if n.isNaN => None
        case _ => Some(d)
      }
    }
  }

  def extractDouble(field: String, data: ACursor): Option[Double] = {
    data.downField(field).as[Double] match {
      case Left(_)  => None
      case Right(d) => d match {
        case n if n.isNaN => None
        case _ => Some(d)
      }
    }
  }

  def extractBoolean(field: String, data: Json): Option[Boolean] = {
    val cursor: HCursor = data.hcursor
    cursor.downField(field).as[Boolean] match {
      case Left(_)  => None
      case Right(b) => Some(b)
    }
  }

  // ------------------- ENVIRONMENT EXTRACTORS --------------------------------

  def extractEnvironment(data: Json): Environment = {
    val cursor: HCursor = data.hcursor
    val jEnv = cursor.downField("environment")
    val name = extractString("name", jEnv).getOrElse("")
    val height = extractInt("height", jEnv).getOrElse(0)
    val width = extractInt("width", jEnv).getOrElse(0)
    val scale = extractDouble("scale", jEnv).getOrElse(10.0)
    val emptyGrid = emptyCellGrid(height, width)
    val grid = extractCellGrid(jEnv, emptyGrid)
    val env = new Environment(name, height, width, scale)
    env.grid = grid
    return env
  }

  def extractCellGrid(jEnv: ACursor, grid: Grid[Cell]): Grid[Cell] = {
    val jGrid = jEnv.downField("grid")
    for (key <- jGrid.keys.getOrElse(Nil)) {
      val cell = extractCell(jGrid.downField(key))
    }
    return grid
  }

  def extractCell(jCell: ACursor): Option[Cell] = {
    val x = extractInt("x", jCell)
    val y = extractInt("y", jCell)
    (x, y) match {
      case (None, _) => None
      case (_, None) => None
      case (Some(x), Some(y)) => {
        val jElements = jCell.downField("elements")
        var elements: MutableMap[String, Element] = MutableMap()
        for (key <- jElements.keys.getOrElse(Nil)) extractElement(key, jElements.downField(key)) match {
          case None => // no element
          case Some(e) => elements += (key -> e)
        }
        val anomalies = jCell.downField("anomalies").as[MutableSet[String]].getOrElse(MutableSet())
        return Some(new Cell(x, y, elements, anomalies))
      }
    }
  }

  def extractElement(elementType: String, jElement: ACursor): Option[Element] = {
    val value = extractDouble("value", jElement)
    elementType match {
      case "Decibel"        => Some(new Decibel(value))
      case "Elevation"      => Some(new Elevation(value))
      case "Latitude"       => Some(new Latitude(value))
      case "Longitude"      => Some(new Longitude(value))
      case "Temperature"    => Some(new Temperature(value))
      case "Water Depth"    => Some(new WaterDepth(value))
      case "Wind Direction" => Some(new WindDirection(value))
      case "Wind Speed"     => Some(new WindSpeed(value))
      case _ => None
    }
  }

  def extractEnvironmentTemplate(data: Json): EnvironmentTemplate = {
    val name = extractString("name", data).getOrElse("")
    val height = extractInt("height", data).getOrElse(0)
    val width = extractInt("width", data).getOrElse(0)
    val scale = extractDouble("scale", data).getOrElse(10.0)
    val elementSeeds = extractElementSeeds(data).getOrElse(Nil)
    val terrainModifications = extractTerrainModifications(data).getOrElse(Nil)
    val anomalies = extractAnomalies(data).getOrElse(Nil)
    return new EnvironmentTemplate(name, height, width, scale, elementSeeds, terrainModifications, anomalies)
  }

  def extractElementSeeds(data: Json): Option[List[ElementSeed]] = {
    val cursor: HCursor = data.hcursor
    val seedsData = cursor.downField("seeds")
    val elementsData = cursor.downField("elements").as[List[String]]
    val seeds: mutable.ListBuffer[ElementSeed] = mutable.ListBuffer()
    elementsData match {
      case Left(_) => None
      case Right(elementTypes) => {
        for (elementType <- elementTypes) createElementSeed(elementType, seedsData) match {
          case Some(seed) => seeds += seed
          case None =>
        }
      }
    }
    return Some(seeds.toList)
  }

  def createElementSeed(elementType: String, seedsData: ACursor): Option[ElementSeed] = {
    val seedData = seedsData.downField(elementType)
    val seedFields = extractSeedFields(seedData)
    seedFields match {
      case None => None
      case Some(seedFields) => elementType match {
        // case "Decibel"        => Some(new DecibelSeed(seedFields))
        case "Elevation"      => Some(new ElevationSeed(seedFields))
        case "Latitude"       => Some(new LatitudeSeed(seedFields))
        case "Longitude"      => Some(new LongitudeSeed(seedFields))
        case "Temperature"    => Some(new TemperatureSeed(seedFields))
        case "Water Depth"    => Some(new WaterDepthSeed(seedFields))
        case "Wind Direction" => Some(new WindDirectionSeed(seedFields))
        case "Wind Speed"     => Some(new WindSpeedSeed(seedFields))
        case _ => None
      }
    }
  }

  def extractTerrainModifications(data: Json): Option[List[TerrainModification]] = {
    val cursor: HCursor = data.hcursor
    val terrainModifications: mutable.ListBuffer[TerrainModification] = mutable.ListBuffer()
    val terrainModificationsData = cursor.downField("terrain-modifications").as[List[Json]]
    terrainModificationsData match {
      case Left(_) => None
      case Right(tms) => {
        for (terrainModification <- tms) createTerrainModification(terrainModification) match {
          case Some(tm) => terrainModifications += tm
          case None =>
        }
      }
    }
    return Some(terrainModifications.toList)
  }

  def createTerrainModification(data: Json): Option[TerrainModification] = {
    val terrainModification: HCursor = data.hcursor
    val terrainModificationType = terrainModification.downField("terrain-modification").as[String]
    terrainModificationType match {
      case Left(_) => None
      case Right(tmt) => {
        val fieldsData = terrainModification.downField("json")
        val fields = extractSeedFields(fieldsData)
        fields match {
          case None => None
          case Some(fields) => tmt match {
            case "Elevation Modification"     => Some(new ElevationModification(fields))
            case "Water Pool Modification"    => Some(new WaterPoolModification(fields))
            case "Water Stream Modification"  => Some(new WaterStreamModification(fields))
            case _ => None
          }
        }
      }
    }
  }

  def extractAnomalies(data: Json): Option[List[Anomaly]] = {
    val cursor: HCursor = data.hcursor
    val anomalies: mutable.ListBuffer[Anomaly] = mutable.ListBuffer()
    val anomaliesData = cursor.downField("anomalies").as[List[Json]]
    anomaliesData match {
      case Left(_) => None
      case Right(anoms) => {
        for (anomaly <- anoms) createAnomaly(anomaly) match {
          case Some(anom) => anomalies += anom
          case None =>
        }
      }
    }
    return Some(anomalies.toList)
  }

  def createAnomaly(data: Json): Option[Anomaly] = {
    val anomaly: HCursor = data.hcursor
    val anomalyType = anomaly.downField("anomaly").as[String]
    anomalyType match {
      case Left(_) => None
      case Right(at) => {
        val fieldsData = anomaly.downField("json")
        val fields = extractSeedFields(fieldsData)
        fields match {
          case None => None
          case Some(fields) => at match {
            case "Human"  => Some(new Human(fields))
            case _ => None
          }
        }
      }
    }
  }

  def extractSeedFields(data: ACursor): Option[Map[String,String]] = data.downField("field-keys").as[List[String]] match {
    case Left(_) => None
    case Right(keys) => {
      val fieldsData = data.downField("fields")
      val keyValueList: mutable.ListBuffer[(String,String)] = mutable.ListBuffer()
      for (key <- keys) extractFieldValue(key, fieldsData) match {
        case Some(keyValue) => keyValueList += keyValue
        case None =>
      }
      return Some(keyValueList.toMap)
    }
  }

  def extractFieldValue(key: String, fields: ACursor): Option[(String,String)] = {
    val fieldData = fields.downField(key)
    fieldData.downField("value").as[String] match {
      case Left(_) => None
      case Right(fieldValue) => Some((key, fieldValue.toString))
    }
  }

  // -------------------- MEMORY EXTRACTORS ------------------------------------

  def extractStateActionMemory(data: Json): AB[StateActionPair] = {
    val stateActionPairs: AB[StateActionPair] = AB()
    val stateActionPairsJson = data.as[List[Json]]
    stateActionPairsJson match {
      case Left(_) => // Extraction failure
      case Right(saps) => for (sap <- saps) stateActionPairs += extractStateActionPair(sap)
    }
    return stateActionPairs
  }

  def extractStateActionPair(data: Json): StateActionPair = {
    val cursor: HCursor = data.hcursor
    val state = extractAgentState(cursor.downField("state"))
    val action = extractString("action", data).getOrElse("")
    val shortTermScore = extractDouble("shortTermScore", data).getOrElse(Double.NaN)
    val longTermScore = extractDouble("longTermScore", data).getOrElse(Double.NaN)
    return new StateActionPair(state, action, shortTermScore, longTermScore)
  }

  def extractAgentState(data: ACursor): AgentState = {
    val xPosition = extractInt("xPosition", data).getOrElse(0)
    val yPosition = extractInt("yPosition", data).getOrElse(0)
    val health = extractDouble("health", data).getOrElse(Double.NaN)
    val energyLevel = extractDouble("energyLevel", data).getOrElse(Double.NaN)
    val elementStatesJson = data.downField("elementStates").as[List[Json]]
    val elementStates = elementStatesJson match {
      case Left(_) => Nil
      case Right(eStates) => (for (state <- eStates) yield extractElementState(state))
    }
    return new AgentState(xPosition, yPosition, health, energyLevel, elementStates)
  }

  def extractElementState(data: Json): ElementState = {
    val cursor: HCursor = data.hcursor
    val elementType = extractString("elementType", data).getOrElse("")
    val indicator = extractBoolean("indicator", data).getOrElse(false)
    val hazard = extractBoolean("hazard", data).getOrElse(false)
    val percentKnownInRange = extractDouble("percentKnownInRange", data).getOrElse(Double.NaN)
    val northQuadrant = extractQuadrantState(cursor.downField("north"))
    val southQuadrant = extractQuadrantState(cursor.downField("south"))
    val westQuadrant = extractQuadrantState(cursor.downField("west"))
    val eastQuadrant = extractQuadrantState(cursor.downField("east"))
    return new ElementState(elementType, indicator, hazard, percentKnownInRange, northQuadrant, southQuadrant, westQuadrant, eastQuadrant)
  }

  def extractQuadrantState(data: ACursor): QuadrantState = {
    val percentKnown = extractDouble("percentKnown", data).getOrElse(Double.NaN)
    val averageValueDifferential = extractDouble("averageValueDifferential", data)
    val immediateValueDifferential = extractDouble("immediateValueDifferential", data)
    return new QuadrantState(percentKnown, averageValueDifferential, immediateValueDifferential)
  }

  // For indexed memory
  // []
  // [STATE, action, sts, lts]
  // [x, y, health, energyLevel, ELEMENTSTATES]
  // [elementType, indicator, hazard, percentKnownInRange, northQuadrant, southQuadrant, westQuadrant, eastQuadrant]
  // [percentKnown, averageValueDifferential, immediateValueDifferential]
  def extractStateActionMemoryIndexed(data: Json): AB[StateActionPair] = {
    val stateActionPairs: AB[StateActionPair] = AB()
    val stateActionPairsJson = data.as[List[Json]]
    stateActionPairsJson match {
      case Left(_) => // Extraction failure
      case Right(saps) => for (sap <- saps) extractStateActionPairIndexed(sap) match {
        case None => // Bad extraction
        case Some(s) => stateActionPairs += s
      }
    }
    return stateActionPairs
  }

  def extractStateActionPairIndexed(data: Json): Option[StateActionPair] = data.as[List[Json]] match {
    case Left(_) => None
    case Right(dataList) => extractAgentStateIndexed(dataList(0)) match {
      case None => None
      case Some(state) => {
        val action = dataList(1).as[String].getOrElse("")
        val shortTermScore = dataList(2).as[Double].getOrElse(Double.NaN)
        val longTermScore = dataList(3).as[Double].getOrElse(Double.NaN)
        return Some(new StateActionPair(state, action, shortTermScore, longTermScore))
      }
    }
  }

  def extractAgentStateIndexed(data: Json): Option[AgentState] = data.as[List[Json]] match {
    case Left(_) => None
    case Right(dataList) => {
      val xPosition = dataList(0).as[Int].getOrElse(0)
      val yPosition = dataList(1).as[Int].getOrElse(0)
      val health = dataList(2).as[Double].getOrElse(Double.NaN)
      val energyLevel = dataList(3).as[Double].getOrElse(Double.NaN)
      val elementStatesJson = dataList(2).as[List[Json]]
      var elementStates: AB[ElementState] = AB()
      elementStatesJson match {
        case Left(_) => Nil
        case Right(eStates) => for (state <- eStates) extractElementStateIndexed(state) match {
          case None => // Bad extraction
          case Some(es) => elementStates += es
        }
      }
      return Some(new AgentState(xPosition, yPosition, health, energyLevel, elementStates.toList))
    }
  }

  def extractElementStateIndexed(data: Json): Option[ElementState] = data.as[List[Json]] match {
    case Left(_) => None
    case Right(dataList) => {
      val elementType = dataList(0).as[String].getOrElse("")
      val indicator = dataList(1).as[Boolean].getOrElse(false)
      val hazard = dataList(2).as[Boolean].getOrElse(false)
      val percentKnownInRange = dataList(3).as[Double].getOrElse(Double.NaN)
      val northQuadrant = extractQuadrantStateIndexed(dataList(4))
      val southQuadrant = extractQuadrantStateIndexed(dataList(5))
      val westQuadrant = extractQuadrantStateIndexed(dataList(6))
      val eastQuadrant = extractQuadrantStateIndexed(dataList(7))
      (northQuadrant, southQuadrant, westQuadrant, eastQuadrant) match {
        case (Some(nq), Some(sq), Some(wq), Some(eq)) => Some(new ElementState(elementType, indicator, hazard, percentKnownInRange, nq, sq, wq, eq))
        case _ => None
      }
    }
  }

  def extractQuadrantStateIndexed(data: Json): Option[QuadrantState] = data.as[List[Json]] match {
    case Left(_) => None
    case Right(dataList) => {
      val percentKnown = dataList(0).as[Double].getOrElse(Double.NaN)
      val averageValueDifferential = dataList(1).as[Double] match {
        case Right(v) => Some(v)
        case _ => None
      }
      val immediateValueDifferential = dataList(2).as[Double] match {
        case Right(v) => Some(v)
        case _ => None
      }
      return Some(new QuadrantState(percentKnown, averageValueDifferential, immediateValueDifferential))
    }
  }

}
