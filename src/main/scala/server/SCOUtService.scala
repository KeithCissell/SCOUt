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
  // List of all seed defaults
  val defaultSeedList: List[ElementSeed] = List(
    DecibleSeed(),
    ElevationSeed(),
    LatitudeSeed(),
    LongitudeSeed(),
    TemperatureSeed(),
    WindDirectionSeed(),
    WindSpeedSeed()
  )
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
    val name = decodeJson("name", data).getOrElse("")
    val length = decodeJson("length", data).getOrElse("")
    val width = decodeJson("width", data).getOrElse("")
    if (name != "" && length != "" && width != "") {
      environment = generateRandomEnvironment("Random Environment", 2, 2, defaultSeedList)
      return Ok(encodeEnvironment(environment))
    } else return BadRequest(req)
    // return input match {
    //   case ("","","") => BadRequest(req)
    //   case (n, l, w) => {
    //     environment = generateRandomEnvironment("Random Environment", 2, 2, defaultSeedList)
    //     Ok(encodeEnvironment(environment))
    //   }
    // }
  }

}
