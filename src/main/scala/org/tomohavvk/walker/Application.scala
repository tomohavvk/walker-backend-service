package org.tomohavvk.walker

import cats.effect.Async
import cats.effect.ExitCode
import cats.effect.kernel.Sync
import cats.effect.std.Console
import cats.implicits._
import cats.mtl.Handle
import cats.~>
import org.tomohavvk.walker.module.Environment
import org.tomohavvk.walker.module.HttpModule
import org.tomohavvk.walker.module.RepositoryModule
import org.tomohavvk.walker.module.ResourceModule
import org.tomohavvk.walker.module.ServiceModule
import org.tomohavvk.walker.module.StreamModule
import org.tomohavvk.walker.persistence.PersistenceMigration
import org.tomohavvk.walker.persistence.Transactor
import org.tomohavvk.walker.protocol.errors.AppError
import org.tomohavvk.walker.utils.LiftConnectionIO
import org.tomohavvk.walker.utils.UnliftF

class Application[F[_]: Async, D[_]: Sync, H[_]: Async: Console](
  implicit environment: Environment[F, D, H],
  transactor:           Transactor[F, D],
  D:                    LiftConnectionIO[D, AppError],
  HF:                   Handle[F, AppError],
  HD:                   Handle[D, AppError],
  U:                    UnliftF[F, H, AppError],
  LiftHF:               H ~> F) {

  import environment.configs
  import environment.loggerF
  import environment.loggerD
  private implicit val loggerH = environment.loggerH

  def run(): F[ExitCode] =
    ResourceModule.make[F](configs).use { implicit resources =>
      for {
        _ <- loggerF.info(s"Starting ${BuildInfo.name} ${BuildInfo.version}...")
        repositories = RepositoryModule.make[D]()
        services     = ServiceModule.make(repositories, transactor, loggerF, loggerD)
        server       = HttpModule.make[F, H](services, environment.codecs, configs.server)
        stream       = StreamModule.make(services, resources, transactor, loggerF)
        _ <- PersistenceMigration.migrate(configs.database, loggerF)
        lifecycle = new Lifecycle[F, D, H](configs, loggerH, server, stream.deviceLocationEventStream)
        exitCode <- LiftHF(lifecycle.start)
      } yield exitCode
    }

}
