package org.tomohavvk.walker.services

import cats.Applicative
import cats.data.EitherT
import cats.data.Kleisli
import cats.effect.kernel.Clock
import cats.effect.kernel.Sync
import cats.implicits.catsSyntaxFlatMapOps
import cats.implicits.catsSyntaxOptionId
import io.odin.Logger
import io.scalaland.chimney.dsl._
import org.tomohavvk.walker.persistence.Transactor
import org.tomohavvk.walker.persistence.repository.DeviceLocationRepository
import org.tomohavvk.walker.protocol.Types.DeviceId
import org.tomohavvk.walker.protocol.Types.UnixTime
import org.tomohavvk.walker.protocol.entities.DeviceLocationEntity
import org.tomohavvk.walker.protocol.errors.NotFoundError
import org.tomohavvk.walker.protocol.views.DeviceLocationView
import org.tomohavvk.walker.utils.ContextFlow
import org.tomohavvk.walker.utils.LogContext
import org.tomohavvk.walker.utils.anySyntax
import org.tomohavvk.walker.utils.liftFSyntax

import java.time.ZoneOffset

class LocationServiceImpl[F[_]: Sync: Clock, B[_]](
  repository: DeviceLocationRepository[B],
  transactor: Transactor[F, B],
  logger:     Logger[ContextFlow[F, *]])
    extends LocationService[F] {

  override def lastLocation(deviceId: DeviceId): ContextFlow[F, DeviceLocationView] = {
    val findFlow: F[Option[DeviceLocationEntity]] = transactor.withTxn(repository.findLastById(deviceId))

    logger.debug("Handle last location request") >>
      findFlow.liftFlow.flatMap {
        case Some(value) => entityToView(value).rightT
        case None =>
          Kleisli.liftF(
            EitherT.leftT[F, DeviceLocationView](NotFoundError(s"Device: ${deviceId.value} not exists in the system"))
          )
      }
  }

  override def upsertBatch(deviceId: DeviceId, locations: List[DeviceLocationEntity]): F[Int] =
    logger
      .debug(s"Handle upserting batch of locations. Size: ${locations.size}")
      .run(LogContext(deviceId = deviceId.some))
      .value >>
      transactor.withTxn {
        repository.upsertBatch {
          locations.sortWith((x, y) => x.time.isBefore(y.time))
        }
      }

  private def entityToView(value: DeviceLocationEntity): DeviceLocationView =
    value
      .into[DeviceLocationView]
      .withFieldComputed(_.bearing, _.bearing.some)
      .withFieldComputed(_.altitudeAccuracy, _.altitudeAccuracy.some)
      .withFieldComputed(_.time, l => UnixTime(l.time.toInstant(ZoneOffset.UTC).toEpochMilli))
      .transform
}
