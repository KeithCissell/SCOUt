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
    case req @ GET  -> Root / "current_state"           => getCurrentState
    case req @ GET  -> Root / "new_random_environment"  => newRandomEnvironment
  }


  // Returns the environment at its current state
  def getCurrentState: Task[Response] = {
    val encode = encodeEnvironment(environment)
    Ok(encodeEnvironment(environment))
  }

  // Sets environment to new randomly generated environment
  def newRandomEnvironment: Task[Response] = {
    environment = generateRandomEnvironment("Random Environment", 2, 2, defaultSeedList)
    Ok(encodeEnvironment(environment))
  }
}
