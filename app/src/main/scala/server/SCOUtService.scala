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
import environment.element.seed._
import environment.EnvironmentBuilder._


object SCOUtService {

  // Holds an initialy empty Environment
  var environment = buildEnvironment("Empty", 2, 2)

  // Mutable list of seeds initialized to default
  var seedList: List[ElementSeed] = SeedList.defaultSeedList

  // Server request handler
  val service = HttpService {
    case req @ GET  -> Root / "ping"                      => Ok("\"pong\"")
    case req @ GET  -> Root / "element_types"             => Ok(encodeMap("Element Types", ElementTypes.elementTypes))
    case req @ POST -> Root / "element_seed_form"         => getElementSeedForm(req)
    case req @ GET  -> Root / "current_state"             => Ok(encodeEnvironment(environment))
    case req @ POST -> Root / "new_random_environment"    => newRandomEnvironment(req)
    case req @ POST -> Root / "build_custom_environment"  => buildCustomEnvironment(req)
  }


  // Gets the form info needed for a specified element type
  def getElementSeedForm(req: Request): Task[Response] = req.decode[Json] { data =>
    val elementType = extractString("element-type", data).getOrElse("")
    if (ElementTypes.elementTypes contains elementType) {
      var responseData = "{}"
      if (elementType == "Elevation") {
        responseData = SeedList.getSeedForm(elementType)
      }
      Ok(responseData)
    } else BadRequest(data)
  }

  // Sets environment to the default environment
  def newRandomEnvironment(req: Request): Task[Response] = req.decode[Json] { data =>
    val name = extractString("name", data).getOrElse("")
    val height = extractInt("height", data).getOrElse(0)
    val width = extractInt("width", data).getOrElse(0)
    (name, height, width) match {
      case ("", _, _) => BadRequest("Bad name")
      case (_, 0, _)  => BadRequest("Bad height")
      case (_, _, 0)  => BadRequest("Bad width")
      case (n, w, h)  => {
        environment = buildEnvironment(n, h, w, SeedList.defaultSeedList)
        Ok(encodeEnvironment(environment))
      }
    }
  }

  // Generates a new environment
  def buildCustomEnvironment(req: Request): Task[Response] = req.decode[Json] { data =>
    val name = extractString("name", data).getOrElse("")
    val height = extractInt("height", data).getOrElse(0)
    val width = extractInt("width", data).getOrElse(0)
    val seeds = extractElementSeeds(data).getOrElse(Nil)
    (name, height, width, seeds) match {
      case ("", _, _, _) => BadRequest("Bad name")
      case (_, 0, _, _)  => BadRequest("Bad height")
      case (_, _, 0, _)  => BadRequest("Bad width")
      case (_, _, _, Nil)  => BadRequest("Bad element seed data")
      case (n, w, h, s)  => {
        seedList = s
        environment = buildEnvironment(n, h, w, s)
        Ok(encodeEnvironment(environment))
      }
    }
  }

}
