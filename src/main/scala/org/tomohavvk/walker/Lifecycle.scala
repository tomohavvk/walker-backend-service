package org.tomohavvk.walker

import cats.Applicative
import cats.Monad
import cats.effect.Async
import cats.effect.ExitCode
import cats.effect.kernel.Spawn
import cats.effect.std.Console
import cats.implicits._
import io.odin.Logger
import org.tomohavvk.walker.config.AppConfig
import org.tomohavvk.walker.config.AppConfig.Env.Dev
import org.tomohavvk.walker.config.AppConfig.Env.Prod
import org.tomohavvk.walker.http.server.HttpServer
import org.tomohavvk.walker.module.Configs
import org.tomohavvk.walker.protocol.errors.AppError
import org.tomohavvk.walker.streams.DeviceLocationEventStream
import org.tomohavvk.walker.utils.UnliftF

class Lifecycle[F[_]: Async, D[_], H[_]: Async: Console](
  configs:     Configs,
  logger:      Logger[H],
  httpServer:  HttpServer[F, H],
  eventStream: DeviceLocationEventStream[F, D]
)(implicit U:  UnliftF[F, H, AppError]) {

  private val startHttpServer: H[Unit] =
    httpServer.start >>
      logger.info("HTTP server finished")

  private val startDeviceLocationStream: H[Unit] =
    U.unlift(eventStream.stream) >> logger.info("Streams finished")

  private val devLoop: H[Unit] =
    logger.info(s"Service running in DEV. Type ENTER to stop it...") >>
      Monad[H].whileM_(Console[H].readLine.map(_ === "\n"))(Applicative[H].unit) >>
      logger.info(s"Service stopping in DEV...")

  private val prodLoop: H[Unit] =
    logger.info(s"Server running in PROD. Send SIGTERM to stop it...") >>
      Async[H].never[Unit]

  private val envLoop: AppConfig => H[Unit] = config => {
    config.env match {
      case Dev  => devLoop
      case Prod => prodLoop
    }
  }

  val start: H[ExitCode] = {
    val streamAndHttp = Spawn[H].race(startHttpServer, startDeviceLocationStream)
    Spawn[H].race(envLoop(configs.app), streamAndHttp).as(ExitCode.Success)
  }

}
