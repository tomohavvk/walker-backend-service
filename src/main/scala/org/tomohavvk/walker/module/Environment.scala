package org.tomohavvk.walker.module

import cats.effect.kernel.Sync
import cats.syntax.functor._
import io.odin.Logger

case class Environment[F[_], D[_], M[_]](
  loggerF: Logger[F],
  loggerD: Logger[D],
  loggerM: Logger[M],
  configs: Configs,
  codecs:  Codecs)

object Environment {

  def make[F[_]: Sync, D[_]: Sync, M[_]: Sync]: F[Either[Exception, Environment[F, D, M]]] =
    for {
      configs <- Configs.make[F]
      loggerF = Logging.makeLogger[F]
      loggerD = Logging.makeLogger[D]
      loggerM = Logging.makeLogger[M]
      codecs  = Codecs.make
    } yield configs.map(configs => Environment(loggerF, loggerD, loggerM, configs, codecs))

}
