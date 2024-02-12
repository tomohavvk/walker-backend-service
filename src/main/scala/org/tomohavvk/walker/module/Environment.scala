package org.tomohavvk.walker.module

import cats.effect.kernel.Sync
import cats.syntax.functor._
import io.odin.Logger
import org.tomohavvk.walker.utils.ContextFlow

case class Environment[F[_], B[_]](
  logger:         Logger[F],
  contextLoggerF: Logger[ContextFlow[F, *]],
  contextLoggerB: Logger[ContextFlow[B, *]],
  configs:        Configs,
  codecs:         Codecs)

object Environment {

  def make[F[_]: Sync, B[_]: Sync]: F[Either[Exception, Environment[F, B]]] =
    for {
      configs <- Configs.make[F]
      logger         = Logging.makeLogger[F]
      contextLoggerF = Logging.makeContext[F]
      contextLoggerB = Logging.makeContext[B]
      codecs         = Codecs.make
    } yield configs.map(configs => Environment(logger, contextLoggerF, contextLoggerB, configs, codecs))

}
