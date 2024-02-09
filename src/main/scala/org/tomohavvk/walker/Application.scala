package org.tomohavvk.walker

import cats.effect.ExitCode
import cats.effect.kernel.Async
import cats.effect.std.Console
import cats.implicits._
import org.tomohavvk.walker.module.EndpointModule
import org.tomohavvk.walker.module.Environment
import org.tomohavvk.walker.module.HttpModule
import org.tomohavvk.walker.module.ResourceModule
import org.tomohavvk.walker.module.RoutesModule
import org.tomohavvk.walker.module.ServiceModule
import org.tomohavvk.walker.module.StreamModule

class Application[F[_]: Async: Console](implicit environment: Environment[F]) {

  import environment.configs
  import environment.logger
  import environment.contextLogger

  def run(): F[ExitCode] =
    ResourceModule.make(configs.deviceLocationEventConsumer).use { implicit resources =>
      for {
        _ <- logger.info(s"Starting ${BuildInfo.name} ${BuildInfo.version}...")

        services  = ServiceModule.make(contextLogger)
        endpoints = EndpointModule.make
        routes    = RoutesModule.make(endpoints, services)
        server    = HttpModule.make(routes)
        stream    = StreamModule.make(resources, logger)
        lifecycle = new Lifecycle[F](configs, logger, server, stream.deviceLocationEventStream)
        exitCode <- lifecycle.start
      } yield exitCode
    }

}
