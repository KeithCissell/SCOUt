// src\main\scala\server\SCOUtService.scala
package server

import io.circe._
import org.http4s._
import org.http4s.dsl._
import org.http4s.circe._
import org.http4s.server._
import scalaz.concurrent.Task

import jsonhandler.Encoder._
import jsonhandler.Decoder._

import environment._
import environment.element._
import environment.anomaly._
import environment.element.seed._
import environment.anomaly.seed._
import environment.EnvironmentBuilder._


object SCOUtService {

  // Holds an initialy empty Environment
  var environment = buildEnvironment("Empty", 2, 2)

  // Mutable list of element seeds initialized to default
  var elementSeedList: List[ElementSeed] = ElementSeedList.defaultSeedList()

  // Mutable list of anomaly seeds initialized to default
  var anomalySeedList: List[AnomalySeed] = AnomalySeedList.defaultSeedList()

  // Server request handler
  val service = HttpService {
    case req @ GET  -> Root / "ping"                      => Ok("\"pong\"")
    case req @ GET  -> Root / "element_types"             => Ok(encodeMap("Element Types", ElementTypes.elementTypes))
    case req @ POST -> Root / "element_seed_form"         => getElementSeedForm(req)
    case req @ POST -> Root / "anomaly_seed_form"         => getAnomalySeedForm(req)
    case req @ GET  -> Root / "current_state"             => Ok(encodeEnvironment(environment))
    case req @ POST -> Root / "new_random_environment"    => newRandomEnvironment(req)
    case req @ POST -> Root / "build_custom_environment"  => buildCustomEnvironment(req)
  }


  // Gets the form info needed for a requested element type
  def getElementSeedForm(req: Request): Task[Response] = req.decode[Json] { data =>
    val elementType = extractString("element-type", data).getOrElse("")
    if (ElementTypes.elementTypes contains elementType) {
      Ok(ElementSeedList.getSeedForm(elementType))
    } else BadRequest(data)
  }

  // Gets the form info needed for a requested anomaly type
  def getAnomalySeedForm(req: Request): Task[Response] = req.decode[Json] { data =>
    val anomalyType = extractString("anomaly-type", data).getOrElse("")
    if (AnomalyTypes.anomalyTypes contains anomalyType) {
      Ok(AnomalySeedList.getSeedForm(anomalyType))
    } else BadRequest(data)
  }

  // Sets environment to the default environment
  def newRandomEnvironment(req: Request): Task[Response] = req.decode[Json] { data =>
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
        anomalySeedList = AnomalySeedList.defaultSeedList()
        environment = buildEnvironment(n, h, w, scale, elementSeedList, anomalySeedList)
        Ok(encodeEnvironment(environment))
      }
    }
  }

  // Generates a new environment
  def buildCustomEnvironment(req: Request): Task[Response] = req.decode[Json] { data =>
    val name = extractString("name", data).getOrElse("")
    val height = extractInt("height", data).getOrElse(0)
    val width = extractInt("width", data).getOrElse(0)
    val scale = extractDouble("scale", data).getOrElse(10.0)
    val elementSeeds = extractElementSeeds(data).getOrElse(Nil)
    val anomalySeeds = extractAnomalySeeds(data).getOrElse(Nil)
    (name, height, width, elementSeeds, anomalySeeds) match {
      case ("", _, _, _, _) => BadRequest("Bad name")
      case (_, 0, _, _, _)  => BadRequest("Bad height")
      case (_, _, 0, _, _)  => BadRequest("Bad width")
      case (_, _, _, Nil, _)  => BadRequest("Bad element seed data")
      case (n, w, h, e, a)  => {
        elementSeedList = e
        anomalySeedList = a
        environment = buildEnvironment(n, h, w, scale, elementSeedList, anomalySeedList)
        Ok(encodeEnvironment(environment))
      }
    }
  }

}
