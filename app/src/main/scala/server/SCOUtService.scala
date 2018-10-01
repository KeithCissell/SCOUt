// src\main\scala\server\SCOUtService.scala
package server

import io.circe._
import io.circe.parser._
import org.http4s._
import org.http4s.dsl._
import org.http4s.circe._
import org.http4s.server._
import scalaz.concurrent.Task
import org.joda.time.DateTime

import jsonhandler.Encoder._
import jsonhandler.Decoder._

import environment._
import environment.anomaly._
import environment.element._
import environment.element.seed._
import environment.terrainmodification._
import environment.EnvironmentBuilder._
import filemanager.FileManager._
// import scoututil.Util._


object SCOUtService {

  // Holds an initialy empty Environment
  var environment = buildEnvironment("Empty", 2, 2)

  // Mutable list of terrain modification seeds initialized to default
  var terrainModificationList: List[TerrainModification] = TerrainModificationList.defaultList()

  // Mutable list of element seeds initialized to default
  var elementSeedList: List[ElementSeed] = ElementSeedList.defaultSeedList()

  // Mutable list of anomaly seeds initialized to default
  var anomalyList: List[Anomaly] = AnomalyList.defaultList()

  // Server request handler
  val service = HttpService {
    case req @ GET  -> Root / "ping"                        => Ok("\"pong\"")
    case req @ GET  -> Root / "element_types"               => Ok(encodeMap("Element Types", ElementTypes.elementTypes))
    case req @ GET  -> Root / "terrain_modification_types"  => Ok(encodeList("Terrain Modification Types", TerrainModificationList.terrainModificationTypes))
    case req @ GET  -> Root / "anomaly_types"               => Ok(encodeList("Anomaly Types", AnomalyList.anomalyTypes))
    case req @ GET  -> Root / "current_state"               => Ok(encodeEnvironment(environment))
    case req @ GET  -> Root / "save_environment"            => saveEnvironment()
    case req @ GET  -> Root / "get_environment_file_list"   => getEnvironmentFileList()
    case req @ GET  -> Root / "get_template_file_list"      => getTemplateFileList()
    case req @ GET  -> Root / "get_operation_file_list"     => getOperationFileList()
    case req @ POST -> Root / "element_seed_form"           => getElementSeedForm(req)
    case req @ POST -> Root / "terrain_modification_form"   => getTerrainModificationForm(req)
    case req @ POST -> Root / "anomaly_form"                => getAnomalyForm(req)
    case req @ POST -> Root / "new_random_environment"      => newRandomEnvironment(req)
    case req @ POST -> Root / "build_custom_environment"    => buildCustomEnvironment(req)
    case req @ POST -> Root / "save_environment_template"   => saveEnvironmentTemplate(req)
    case req @ POST -> Root / "get_environment_file"        => getEnvironmentFile(req)
    case req @ POST -> Root / "get_template_file"           => getTemplateFile(req)
    case req @ POST -> Root / "get_operation_file"          => getOperationFile(req)
  }


  // Gets the form info needed for a requested element type
  def getElementSeedForm(req: Request): Task[Response] = parse(req.bodyAsText.runLastOr("").run) match {
    case Left(_) => BadRequest()
    case Right(data) => {
      val elementType = extractString("element-type", data).getOrElse("")
      if (ElementTypes.elementTypes contains elementType) {
        Ok(ElementSeedList.getSeedForm(elementType))
      } else BadRequest(data)
    }
  }

  // Gets the form info needed for a requested terrain modification type
  def getTerrainModificationForm(req: Request): Task[Response] = parse(req.bodyAsText.runLastOr("").run) match {
    case Left(_) => BadRequest()
    case Right(data) => {
      val terrainModificationType = extractString("terrain-modification-type", data).getOrElse("")
      if (TerrainModificationList.terrainModificationTypes contains terrainModificationType) {
        Ok(TerrainModificationList.getForm(terrainModificationType))
      } else BadRequest(data)
    }
  }

  // Gets the form info needed for a requested anomaly type
  def getAnomalyForm(req: Request): Task[Response] = parse(req.bodyAsText.runLastOr("").run) match {
    case Left(_) => BadRequest()
    case Right(data) => {
      val anomalyType = extractString("anomaly-type", data).getOrElse("")
      if (AnomalyList.anomalyTypes contains anomalyType) {
        Ok(AnomalyList.getForm(anomalyType))
      } else BadRequest(data)
    }
  }

  // Sets environment to the default environment
  def newRandomEnvironment(req: Request): Task[Response] = parse(req.bodyAsText.runLastOr("").run) match {
    case Left(_) => BadRequest()
    case Right(data) => {
      println(data.toString)
      val name = extractString("name", data).getOrElse("")
      val height = extractInt("height", data).getOrElse(0)
      val width = extractInt("width", data).getOrElse(0)
      val scale = extractDouble("scale", data).getOrElse(10.0)
      (name, height, width) match {
        case ("", _, _) => BadRequest("Bad name")
        case (_, 0, _)  => BadRequest("Bad height")
        case (_, _, 0)  => BadRequest("Bad width")
        case (n, w, h)  => {
          elementSeedList = ElementSeedList.defaultSeedList()
          terrainModificationList = TerrainModificationList.defaultList()
          anomalyList = AnomalyList.defaultList()
          environment = buildEnvironment(n, h, w, scale, elementSeedList, terrainModificationList, anomalyList)
          Ok(encodeEnvironment(environment))
        }
      }
    }
  }

  // Generates a new environment
  def buildCustomEnvironment(req: Request): Task[Response] = parse(req.bodyAsText.runLastOr("").run) match {
    case Left(_) => BadRequest()
    case Right(data) => {
      val template = extractEnvironmentTemplate(data)
      (template.name, template.height, template.width, template.elementSeeds, template.terrainModifications, template.anomalies) match {
        case ("", _, _, _, _, _)  => BadRequest("Bad name")
        case (_, 0, _, _, _, _)   => BadRequest("Bad height")
        case (_, _, 0, _, _, _)   => BadRequest("Bad width")
        case (_, _, _, Nil, _, _) => BadRequest("Bad element seed data")
        case (n, w, h, e, t, a)   => {
          elementSeedList = e
          terrainModificationList = t
          anomalyList = a
          environment = buildEnvironment(template)
          Ok(encodeEnvironment(environment))
        }
      }
    }
  }

  // Saves an environmentTemplate to a json file
  def saveEnvironmentTemplate(req: Request): Task[Response] = parse(req.bodyAsText.runLastOr("").run) match {
    case Left(_) => BadRequest()
    case Right(data) => {
      val name = extractString("name", data).getOrElse("NameNotFound")
      val jsonString = data.toString
      val fileName = new DateTime().toString("yyyy-MM-dd-HH-mm") + "-" + name
      saveJsonFile(fileName, "src/resources/environment-templates/", jsonString)
      Ok(s"Saved as $fileName")
    }
  }

  // Saves the environment to a json file
  def saveEnvironment(): Task[Response] = {
    val fileName = new DateTime().toString("yyyy-MM-dd-HH-mm") + "-" + environment.name
    val encodedEnv = encodeEnvironment(environment)
    saveJsonFile(fileName, "src/resources/environments/", encodedEnv)
    Ok(s"Saved as $fileName")
  }

  // Environment Files
  def getEnvironmentFileList(): Task[Response] = {
    val fileNames = getEnvironmentFileNames()
    val jsonResponse = Json.fromValues(getEnvironmentFileNames().map(fn => Json.fromString(fn)))
    Ok(jsonResponse.toString)
  }

  def getEnvironmentFile(req: Request): Task[Response] = parse(req.bodyAsText.runLastOr("").run) match {
    case Left(_) => BadRequest()
    case Right(data) => {
      extractString("fileName", data) match {
        case None => BadRequest()
        case Some(fName) => fileExists(fName, environmentPath, "json") match {
          case false => BadRequest("File Does Not Exist")
          case true => Ok(readJsonFile(fName, environmentPath))
        }
      }
    }
  }

  // Environment Template Files
  def getTemplateFileList(): Task[Response] = {
    val fileNames = getEnvironmentFileNames()
    val jsonResponse = Json.fromValues(getEnvironmentTemplateFileNames().map(fn => Json.fromString(fn)))
    Ok(jsonResponse.toString)
  }

  def getTemplateFile(req: Request): Task[Response] = parse(req.bodyAsText.runLastOr("").run) match {
    case Left(_) => BadRequest()
    case Right(data) => {
      extractString("fileName", data) match {
        case None => BadRequest()
        case Some(fName) => fileExists(fName, environmentTemplatePath, "json") match {
          case false => BadRequest("File Does Not Exist")
          case true => Ok(readJsonFile(fName, environmentTemplatePath))
        }
      }
    }
  }

  // Operation Run Files
  def getOperationFileList(): Task[Response] = {
    val fileNames = getEnvironmentFileNames()
    val jsonResponse = Json.fromValues(getOperationRunFileNames().map(fn => Json.fromString(fn)))
    Ok(jsonResponse.toString)
  }

  def getOperationFile(req: Request): Task[Response] = parse(req.bodyAsText.runLastOr("").run) match {
    case Left(_) => BadRequest()
    case Right(data) => {
      extractString("fileName", data) match {
        case None => BadRequest()
        case Some(fName) => fileExists(fName, operationRunPath, "json") match {
          case false => BadRequest("File Does Not Exist")
          case true => Ok(readJsonFile(fName, operationRunPath))
        }
      }
    }
  }

}
