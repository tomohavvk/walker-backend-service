package org.tomohavvk.walker.generation

import cats.effect.kernel.Sync
import com.aventrix.jnanoid.jnanoid.NanoIdUtils

trait NanoIdGen[F[_]] {
  def randomNanoId: F[String]
}

object NanoIdGen {
  def apply[F[_]](implicit ev: NanoIdGen[F]): NanoIdGen[F] = ev

  implicit def fromSync[F[_]: Sync]: NanoIdGen[F] =
    new NanoIdGen[F] {
      override def randomNanoId: F[String] = Sync[F].delay(NanoIdUtils.randomNanoId())
    }
  def randomNanoId[F[_]: NanoIdGen]: F[String] = NanoIdGen[F].randomNanoId
}
