package org.tomohavvk.walker

import cats.effect.Async
import cats.effect.ExitCode
import cats.effect.std.Console
import cats.implicits._
import org.tomohavvk.walker.module.EndpointModule
import org.tomohavvk.walker.module.Environment
import org.tomohavvk.walker.module.HttpModule
import org.tomohavvk.walker.module.RepositoryModule
import org.tomohavvk.walker.module.ResourceModule
import org.tomohavvk.walker.module.RoutesModule
import org.tomohavvk.walker.module.ServiceModule
import org.tomohavvk.walker.module.StreamModule

import org.tomohavvk.walker.persistence.PersistenceMigration
import org.tomohavvk.walker.persistence.Transactor
import org.tomohavvk.walker.protocol.errors.AppError
import org.tomohavvk.walker.utils.LiftConnectionIO

class Application[F[_]: Async: Console, B[_] /*, C[_]: Async: MonadCancelThrow*/ ](
  implicit environment: Environment[F],
  transactor:           Transactor[F, B]
  /*LiftMF:               C ~> F*/) {

  import environment.configs
  import environment.logger
  import environment.contextLogger

  def run()(implicit F: LiftConnectionIO[B, AppError]): F[ExitCode] =
//    val appTransactor = new PostgresTransactor[F, B, C](transactor)
    ResourceModule.make[F](configs).use { implicit resources =>
      for {
        _ <- logger.info(s"Starting ${BuildInfo.name} ${BuildInfo.version}...")
        repositories = RepositoryModule.make[B]()
        services     = ServiceModule.make(repositories, transactor, contextLogger)
        endpoints    = EndpointModule.make
        routes       = RoutesModule.make(endpoints, services)
        server       = HttpModule.make(routes)
        stream       = StreamModule.make(repositories, resources, transactor, logger)
        _ <- PersistenceMigration.migrate(configs.database, logger)
        lifecycle = new Lifecycle[F, B](configs, logger, server, stream.deviceLocationEventStream)
        exitCode <- lifecycle.start
      } yield exitCode
    }

}
