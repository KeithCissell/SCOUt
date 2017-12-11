package jsonhandler

import io.circe._
import environment.element.seed._
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
        // case "Latitude"       => Some(new LatitudeSeed(seedFields))
        // case "Longitude"      => Some(new LongitudeSeed(seedFields))
        // case "Temperature"    => Some(new TemperatureSeed(seedFields))
        // case "Wind Direction" => Some(new WindDirectionSeed(seedFields))
        // case "Wind Speed"     => Some(new WindSpeedSeed(seedFields))
        case _ => None
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
