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
  var seedList: List[ElementSeed] = DefaultSeedList.defaultSeedList

  // Server request handler
  val service = HttpService {
    case req @ GET  -> Root / "ping"                    => Ok("\"pong\"")
    case req @ GET  -> Root / "element_types"           => Ok(encodeMap("Element Types", ElementTypes.elementTypes))
    case req @ POST -> Root / "element_seed_form"       => getElementSeedForm(req)
    case req @ GET  -> Root / "current_state"           => Ok(encodeEnvironment(environment))
    case req @ POST -> Root / "new_random_environment"  => newRandomEnvironment(req)
  }


  // Gets the form info needed for a specified element type
  def getElementSeedForm(req: Request): Task[Response] = req.decode[Json] { data =>
    val elementType = extractString("element-type", data).getOrElse("")
    if (ElementTypes.elementTypes contains elementType) {
      println("Yay*******************************")
      Ok(data)
    } else BadRequest(data)
  }

  // Sets environment to the default environment
  def newRandomEnvironment(req: Request): Task[Response] = req.decode[Json] { data =>
    val name = extractString("name", data).getOrElse("")
    val length = extractInt("length", data).getOrElse(0)
    val width = extractInt("width", data).getOrElse(0)
    (name, length, width) match {
      case ("", 0, 0) => BadRequest(data)
      case (n, w, l)  => {
        environment = buildEnvironment(n, w, l, DefaultSeedList.defaultSeedList)
        Ok(encodeEnvironment(environment))
      }
    }
  }

}
