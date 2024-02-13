package org.tomohavvk.walker.http.server

import cats.effect.ExitCode
import cats.effect.kernel.Async
import cats.implicits._
import fs2.concurrent.SignallingRef
import org.http4s.HttpRoutes
import org.http4s.blaze.server.BlazeServerBuilder
import org.tomohavvk.walker.config.ServerConfig

class HttpServer[H[_]: Async](serverConfig: ServerConfig, routes: HttpRoutes[H]) {

  def start: H[ExitCode] =
    for {
      terminationRef <- SignallingRef[H, Boolean](initial = false)
      exitWith       <- SignallingRef[H, ExitCode](ExitCode.Success)
      exitCode       <- runServer(terminationRef, exitWith)
    } yield exitCode

  private def runServer(terminationRef: SignallingRef[H, Boolean], exitWith: SignallingRef[H, ExitCode]): H[ExitCode] =
    BlazeServerBuilder[H]
      .bindHttp(port = serverConfig.port, host = serverConfig.host)
      .withHttpApp(routes.orNotFound)
      .serveWhile(terminationRef, exitWith)
      .compile
      .drain
      .as(ExitCode.Success)

}
