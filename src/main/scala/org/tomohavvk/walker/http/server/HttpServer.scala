package org.tomohavvk.walker.http.server

import cats.effect.ExitCode
import cats.effect.kernel.Async
import cats.implicits._
import fs2.concurrent.SignallingRef
import org.http4s.HttpRoutes
import org.http4s.blaze.server.BlazeServerBuilder
import org.tomohavvk.walker.config.ServerConfig
import org.tomohavvk.walker.http.routes.api.WalkerWSApi

class HttpServer[F[_], M[_]: Async](serverConfig: ServerConfig, routes: HttpRoutes[M], walkerWSApi: WalkerWSApi[F, M]) {

  def start: M[ExitCode] =
    for {
      terminationRef <- SignallingRef[M, Boolean](initial = false)
      exitWith       <- SignallingRef[M, ExitCode](ExitCode.Success)
      exitCode       <- runServer(terminationRef, exitWith)
    } yield exitCode

  private def runServer(terminationRef: SignallingRef[M, Boolean], exitWith: SignallingRef[M, ExitCode]): M[ExitCode] =
    BlazeServerBuilder[M]
      .bindHttp(port = serverConfig.port, host = serverConfig.host)
      .withHttpWebSocketApp(wsb => (routes <+> walkerWSApi.wsRoute(wsb)).orNotFound)
      .serveWhile(terminationRef, exitWith)
      .compile
      .drain
      .as(ExitCode.Success)

}
