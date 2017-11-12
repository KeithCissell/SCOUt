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
import environment.generator.ElementSeeds._
import environment.generator.RandomGenerator._


object SCOUtService {

  // Holds an initialy empty Environment
  var environment = generateRandomEnvironment("Empty", 2, 2)
  
  // Mutable list of seeds
  var seedList: List[ElementSeed] = defaultSeedList

  // Server request handler
  val service = HttpService {
    case req @ GET  -> Root / "ping"                    => Ok("\"pong\"")
    case req @ GET  -> Root / "current_state"           => Ok(encodeEnvironment(environment))
    case req @ POST -> Root / "new_random_environment"  => newRandomEnvironment(req)
  }

  // Sets environment to new randomly generated environment
  def newRandomEnvironment(req: Request): Task[Response] = req.decode[Json] { data =>
    val name = extractString("name", data).getOrElse("")
    val length = extractInt("length", data).getOrElse(0)
    val width = extractInt("width", data).getOrElse(0)
    (name, length, width) match {
      case ("", 0, 0) => BadRequest(data)
      case (n, w, l)  => {
        environment = generateRandomEnvironment(n, w, l, defaultSeedList)
        Ok(encodeEnvironment(environment))
      }
    }
  }

}
