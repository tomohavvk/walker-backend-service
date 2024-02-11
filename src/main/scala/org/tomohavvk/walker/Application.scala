package org.tomohavvk.walker

import cats.effect.kernel.MonadCancelThrow
import cats.effect.{Async, Concurrent, ExitCode, Resource, Spawn}
import cats.effect.std.Console
import cats.implicits._
import org.tomohavvk.walker.module.{DBDeps, DBModule, EndpointModule, Environment, HttpModule, RepositoryModule, ResourceModule, ResourcesDeps, RoutesModule, ServiceModule, StreamModule}
import cats.{Applicative, ~>}
import doobie.hikari.HikariTransactor
import org.tomohavvk.walker.persistence.{PersistenceMigration, PostgresTransactor, Transactor}
import org.tomohavvk.walker.protocol.errors.AppError
import org.tomohavvk.walker.utils.{LiftConnectionIO, TransactConnectionIO}

class Application[F[_]: Async: Console, B[_], C[_]: Async: MonadCancelThrow](implicit environment: Environment[F], transactor: HikariTransactor[C], LiftMF: C ~> F) {

  import environment.configs
  import environment.logger
  import environment.contextLogger

  def run()(implicit F: LiftConnectionIO[B, AppError], T: TransactConnectionIO[F, B, C]): F[ExitCode] = {

    val appTransactor = new PostgresTransactor[F, B, C](transactor)

    ResourceModule.make[F](configs).use { implicit resources =>
        for {
          _ <- logger.info(s"Starting ${BuildInfo.name} ${BuildInfo.version}...")

          repositories = RepositoryModule.make[B]()
          services = ServiceModule.make(repositories, appTransactor, contextLogger)
          endpoints = EndpointModule.make
          routes = RoutesModule.make(endpoints, services)
          server = HttpModule.make(routes)
          stream = StreamModule.make(repositories, resources, appTransactor, logger)
          _ <- PersistenceMigration.migrate(configs.database, logger)
          lifecycle = new Lifecycle[F, B](configs, logger, server, stream.deviceLocationEventStream)
          exitCode <- lifecycle.start
        } yield exitCode
      }

}
}
