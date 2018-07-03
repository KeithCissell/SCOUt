package server

import java.util.concurrent.{ExecutorService, Executors}

import scala.util.Properties.envOrNone

import scalaz.concurrent.Task
import scala.concurrent.duration.Duration

import org.http4s.server.{Server, ServerApp, IdleTimeoutSupport}
import org.http4s.server.blaze.BlazeBuilder


object SCOUtServer extends ServerApp {

  val port : Int              = envOrNone("HTTP_PORT") map (_.toInt) getOrElse 8080
  val ip   : String           = "0.0.0.0"
  val pool : ExecutorService  = Executors.newCachedThreadPool()
  val idle : Duration         = Duration(5, "minutes")

  override def server(args: List[String]): Task[Server] =
    BlazeBuilder
      .bindHttp(port, ip)
      .mountService(SCOUtService.service)
      .withServiceExecutor(pool)
      .withIdleTimeout(idle)
      .start
}
