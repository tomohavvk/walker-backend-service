package org.tomohavvk.walker

import cats.Applicative
import cats.Monad
import cats.effect.Async
import cats.effect.ExitCode
import cats.effect.kernel.Spawn
import cats.effect.std.Console
import cats.implicits._
import org.tomohavvk.walker.config.AppConfig.Env.Dev
import org.tomohavvk.walker.config.AppConfig.Env.Prod
import io.odin.Logger
import org.tomohavvk.walker.config.AppConfig
import org.tomohavvk.walker.http.server.HttpServer
import org.tomohavvk.walker.module.Configs
import org.tomohavvk.walker.streams.DeviceLocationEventStream

class Lifecycle[F[_]: Async: Console](
  configs:     Configs,
  logger:      Logger[F],
  httpServer:  HttpServer[F],
  eventStream: DeviceLocationEventStream[F]) {

  private val startHttpServer: F[Unit] =
    httpServer.start >>
      logger.info("HTTP server finished")

  private val startDeviceLocationStream: F[Unit] =
    eventStream.stream >> logger.info("Streams finished")

  private val devLoop: F[Unit] =
    logger.info(s"Service running in DEV. Type ENTER to stop it...") >>
      Monad[F].whileM_(Console[F].readLine.map(_ === "\n"))(Applicative[F].unit) >>
      logger.info(s"Service stopping in DEV...")

  private val prodLoop: F[Unit] =
    logger.info(s"Server running in PROD. Send SIGTERM to stop it...") >>
      Async[F].never[Unit]

  private val envLoop: AppConfig => F[Unit] = config => {
    config.env match {
      case Dev  => devLoop
      case Prod => prodLoop
    }
  }

  val start: F[ExitCode] = {
    val streamAndHttp = Spawn[F].race(startHttpServer, startDeviceLocationStream)
    Spawn[F].race(envLoop(configs.app), streamAndHttp).as(ExitCode.Success)
  }

}
