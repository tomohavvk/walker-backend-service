package org.tomohavvk.walker.http.server

import cats.effect.ExitCode
import cats.effect.kernel.Async
import cats.implicits._
import fs2.concurrent.SignallingRef
import org.http4s.blaze.server.BlazeServerBuilder
import org.tomohavvk.walker.config.ServerConfig
import org.tomohavvk.walker.http.routes.RoutesDeps

class HttpServer[F[_]: Async](serverConfig: ServerConfig, routes: RoutesDeps[F]) {

  def start: F[ExitCode] =
    for {
      terminationRef <- SignallingRef[F, Boolean](initial = false)
      exitWith       <- SignallingRef[F, ExitCode](ExitCode.Success)
      exitCode       <- runServer(terminationRef, exitWith)
    } yield exitCode

  private def runServer(terminationRef: SignallingRef[F, Boolean], exitWith: SignallingRef[F, ExitCode]): F[ExitCode] =
    BlazeServerBuilder[F]
      .bindHttp(port = serverConfig.port, host = serverConfig.host)
      .withHttpApp(routes.apiRoutes.orNotFound)
      .serveWhile(terminationRef, exitWith)
      .compile
      .drain
      .as(ExitCode.Success)

}
