package org.tomohavvk.walker.module

import cats.effect.kernel.Sync
import cats.syntax.functor._
import io.odin.Logger
import org.tomohavvk.walker.utils.ContextFlow

case class Environment[F[_]](
  logger:        Logger[F],
  contextLogger: Logger[ContextFlow[F, *]],
  configs:       Configs,
  codecs:        Codecs)

object Environment {

  def make[F[_]: Sync]: F[Either[Exception, Environment[F]]] =
    for {
      configs <- Configs.make
      logger        = Logging.makeLogger
      contextLogger = Logging.makeContext
      codecs        = Codecs.make
    } yield configs.map(configs => Environment(logger, contextLogger, configs, codecs))

}
