package org.tomohavvk.walker

import cats.effect.Async
import cats.effect.ExitCode
import cats.effect.kernel.Sync
import cats.effect.std.Console
import cats.implicits._
import cats.mtl.Handle
import cats.~>
import io.odin.Logger
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

class Application[F[_]: Async, D[_]: Sync, M[_]: Async: Console](
  implicit environment: Environment[F, D, M],
  transactor:           Transactor[F, D],
  D:                    LiftConnectionIO[D, AppError],
  HF:                   Handle[F, AppError],
  HD:                   Handle[D, AppError],
  U:                    UnliftF[F, M, AppError],
  LiftHF:               M ~> F) {

  import environment.configs
  private implicit val loggerF: Logger[F] = environment.loggerF
  private implicit val loggerM: Logger[M] = environment.loggerM

  def run(): F[ExitCode] =
    ResourceModule.make[F](configs).use { implicit resources =>
      for {
        _ <- loggerF.info(s"Starting ${BuildInfo.name} ${BuildInfo.version}...")
        repositories = RepositoryModule.make[D]()
        services     = ServiceModule.make(repositories, transactor, loggerF)
        server <- HttpModule.make[F, M](services, environment.codecs, configs.server)
        stream = StreamModule.make[F, D](services, resources)
        _ <- PersistenceMigration.migrate(configs.database, loggerF)
        lifecycle = new Lifecycle[F, D, M](configs, loggerM, server, stream.deviceLocationEventStream)
        exitCode <- LiftHF(lifecycle.start)
      } yield exitCode
    }

}
