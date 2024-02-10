package org.tomohavvk.walker

import cats.effect.kernel.MonadCancelThrow
import cats.effect.{Async, ExitCode, Resource}
import cats.effect.std.Console
import cats.implicits._
import org.tomohavvk.walker.module.{DBDeps, DBModule, EndpointModule, Environment, HttpModule, RepositoryModule, ResourceModule, ResourcesDeps, RoutesModule, ServiceModule, StreamModule}
import cats.{Applicative, ~>}
import org.tomohavvk.walker.persistence.{PersistenceMigration, PostgresTransactor}
import org.tomohavvk.walker.protocol.errors.AppError
import org.tomohavvk.walker.utils.{LiftConnectionIO, TransactConnectionIO}

class Application[F[_]: Async: Console, B[_], C[_]: Async: MonadCancelThrow](implicit environment: Environment[F], LiftMF:            C ~> F) {

  import environment.configs
  import environment.logger
  import environment.contextLogger

  def run()(implicit F: LiftConnectionIO[B, AppError], T: TransactConnectionIO[F, B, C]): F[ExitCode] = {
    val resourceF: Resource[F, ResourcesDeps[F]] = ResourceModule.make[F](configs)

    val resourceB: C[PostgresTransactor[F, B, C]] = DBModule.make[C](configs).use { dbDeps =>
    val  transactor = new PostgresTransactor[F, B, C](dbDeps.transactor)

      Applicative[C].pure(transactor)
    }

    val liftedResourceB: F[PostgresTransactor[F, B, C]] =LiftMF {resourceB }


    resourceF.use { implicit resources =>
        for {
          _ <- logger.info(s"Starting ${BuildInfo.name} ${BuildInfo.version}...")
          transactor <- liftedResourceB

          repositories = RepositoryModule.make[B]()
          services = ServiceModule.make(repositories, transactor, contextLogger)
          endpoints = EndpointModule.make
          routes = RoutesModule.make(endpoints, services)
          server = HttpModule.make(routes)
          stream = StreamModule.make(repositories, resources,transactor, logger)
          _ <- PersistenceMigration.migrate(configs.database, logger)
          lifecycle = new Lifecycle[F, B](configs, logger, server, stream.deviceLocationEventStream)
          exitCode <- lifecycle.start
        } yield exitCode
      }

}
}
