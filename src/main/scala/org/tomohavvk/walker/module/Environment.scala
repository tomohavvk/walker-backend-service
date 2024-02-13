package org.tomohavvk.walker.module

import cats.effect.kernel.Sync
import cats.syntax.functor._
import io.odin.Logger

case class Environment[F[_], D[_], H[_]](
  loggerF: Logger[F],
  loggerD: Logger[D],
  loggerH: Logger[H],
  configs: Configs,
  codecs:  Codecs)

object Environment {

  def make[F[_]: Sync, B[_]: Sync, H[_]: Sync]: F[Either[Exception, Environment[F, B, H]]] =
    for {
      configs <- Configs.make[F]
      loggerF = Logging.makeLogger[F]
      loggerD = Logging.makeLogger[B]
      loggerH = Logging.makeLogger[H]
      codecs  = Codecs.make
    } yield configs.map(configs => Environment(loggerF, loggerD, loggerH, configs, codecs))

}
