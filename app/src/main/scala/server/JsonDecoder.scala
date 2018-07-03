package jsonhandler

import io.circe._
import environment.element.seed._
import environment.anomaly._
import environment.terrainmodification._
import scala.collection.mutable


object Decoder {

  def extractString(field: String, data: Json): Option[String] = {
    val cursor: HCursor = data.hcursor
    cursor.downField(field).as[String] match {
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

  def extractDouble(field: String, data: Json): Option[Double] = {
    val cursor: HCursor = data.hcursor
    cursor.downField(field).as[Double] match {
      case Left(_)  => None
      case Right(i) => Some(i)
    }
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

}
