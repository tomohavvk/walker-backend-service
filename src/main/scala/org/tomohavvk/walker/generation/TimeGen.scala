package org.tomohavvk.walker.generation

import cats.effect.kernel.Clock
import cats.effect.kernel.Sync
import cats.syntax.functor._

import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset

trait TimeGen[F[_]] {
  def genTimeUtc: F[LocalDateTime]
}

object TimeGen {

  def apply[F[_]](implicit ev: TimeGen[F]): TimeGen[F] = ev

  implicit def fromSync[F[_]](implicit ev: Sync[F]): TimeGen[F] =
    new TimeGen[F] {

      override def genTimeUtc: F[LocalDateTime] =
        Clock[F].realTime
          .map(_.toMillis)
          .map(millis => Instant.ofEpochMilli(millis))
          .map(instant => LocalDateTime.ofInstant(instant, ZoneOffset.UTC))
    }
  def genTimeUtc[F[_]: TimeGen]: F[LocalDateTime] = TimeGen[F].genTimeUtc
}
