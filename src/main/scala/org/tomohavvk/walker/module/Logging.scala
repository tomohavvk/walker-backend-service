package org.tomohavvk.walker.module

import cats.effect.kernel.Sync
import io.odin._
import io.odin.formatter.Formatter
import io.odin.formatter.options.PositionFormat
import io.odin.formatter.options.ThrowableFormat

object Logging {

  private val formatter: Formatter =
    Formatter.create(ThrowableFormat.Default, PositionFormat.AbbreviatePackage, colorful = true, printCtx = true)

  def makeLogger[F[_]: Sync]: Logger[F] = consoleLogger[F](formatter = formatter).withMinimalLevel(Level.Debug)

}
