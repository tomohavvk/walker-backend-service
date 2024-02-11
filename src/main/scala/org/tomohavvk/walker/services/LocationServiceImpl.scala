package org.tomohavvk.walker.services

import cats.Applicative
import cats.MonadError
import cats.effect.kernel.Clock
import cats.effect.kernel.Sync
import cats.implicits.catsSyntaxFlatMapOps
import cats.implicits.catsSyntaxOptionId
import cats.implicits.toFlatMapOps
import io.odin.Logger
import io.scalaland.chimney.dsl._
import org.tomohavvk.walker.persistence.Transactor
import org.tomohavvk.walker.persistence.repository.DeviceLocationRepository
import org.tomohavvk.walker.protocol.Types.DeviceId
import org.tomohavvk.walker.protocol.Types.UnixTime
import org.tomohavvk.walker.protocol.entities.DeviceLocationEntity
import org.tomohavvk.walker.protocol.views.DeviceLocationView
import org.tomohavvk.walker.utils.ContextFlow
import org.tomohavvk.walker.utils.liftFSyntax

import java.time.ZoneOffset

class LocationServiceImpl[F[_]: Sync: Clock, B[_]](
  repository: DeviceLocationRepository[B],
  transactor: Transactor[F, B],
  logger:     Logger[ContextFlow[F, *]])
    extends LocationService[F] {

  override def lastLocation(deviceId: DeviceId): ContextFlow[F, DeviceLocationView] = {
    val findFlow: F[Option[DeviceLocationEntity]] = transactor.withTxn(repository.findLastById(deviceId))

    val result: ContextFlow[F, DeviceLocationView] =
      logger.info("Handle last location request") >>
        findFlow.flatMap {
          case Some(value) =>
            Applicative[F].pure(
              value
                .into[DeviceLocationView]
                .withFieldComputed(_.bearing, _.bearing.some)
                .withFieldComputed(_.altitudeAccuracy, _.altitudeAccuracy.some)
                .withFieldComputed(_.time, l => UnixTime(l.time.toInstant(ZoneOffset.UTC).toEpochMilli))
                .transform
            )
          case None => MonadError[F, Throwable].raiseError[DeviceLocationView](new RuntimeException("ENTITY NOT FOUND"))
        }.liftFlow

    result
  }
}
