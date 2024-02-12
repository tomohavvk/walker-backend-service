package org.tomohavvk.walker.services

import cats.data.EitherT
import cats.data.Kleisli
import cats.effect.kernel.Clock
import cats.effect.kernel.Sync
import cats.implicits.catsSyntaxFlatMapOps
import cats.implicits.catsSyntaxOptionId
import cats.implicits.toFlatMapOps
import cats.implicits.toFunctorOps
import cats.mtl.Handle
import cats.mtl.implicits.toHandleOps
import io.odin.Logger
import io.scalaland.chimney.dsl._
import org.tomohavvk.walker.generation.TimeGen
import org.tomohavvk.walker.persistence.Transactor
import org.tomohavvk.walker.persistence.repository.DeviceLocationRepository
import org.tomohavvk.walker.persistence.repository.DeviceRepository
import org.tomohavvk.walker.protocol.Types.DeviceId
import org.tomohavvk.walker.protocol.Types.DeviceName
import org.tomohavvk.walker.protocol.Types.UnixTime
import org.tomohavvk.walker.protocol.entities.DeviceEntity
import org.tomohavvk.walker.protocol.entities.DeviceLocationEntity
import org.tomohavvk.walker.protocol.errors.AppError
import org.tomohavvk.walker.protocol.errors.NotFoundError
import org.tomohavvk.walker.protocol.errors.ViolatesForeignKeyError
import org.tomohavvk.walker.protocol.views.DeviceLocationView
import org.tomohavvk.walker.utils.ContextFlow
import org.tomohavvk.walker.utils.LogContext
import org.tomohavvk.walker.utils.anySyntax
import org.tomohavvk.walker.utils.liftFSyntax

import java.time.ZoneOffset

trait LocationService[F[_]] {
  def lastLocation(deviceId: DeviceId): ContextFlow[F, DeviceLocationView]
  def upsertBatch(deviceId:  DeviceId, locations: List[DeviceLocationEntity]): F[Int]
}

class LocationServiceImpl[F[_]: Sync: Clock, B[_]: Sync](
  deviceRepo:         DeviceRepository[B],
  deviceLocationRepo: DeviceLocationRepository[B],
  transactor:         Transactor[F, B],
  loggerF:            Logger[ContextFlow[F, *]],
  loggerB:            Logger[ContextFlow[B, *]]
)(implicit H:         Handle[F, AppError])
    extends LocationService[F] {

  override def lastLocation(deviceId: DeviceId): ContextFlow[F, DeviceLocationView] = {
    val findFlow: F[Option[DeviceLocationEntity]] = transactor.withTxn(deviceLocationRepo.findLastById(deviceId))

    loggerF.debug("Last location request") >>
      findFlow.liftFlow.flatMap {
        case Some(value) => entityToView(value).rightT
        case None =>
          Kleisli.liftF(
            EitherT.leftT[F, DeviceLocationView](NotFoundError(s"Device: ${deviceId.value} not exists in the system"))
          )
      }
  }

  override def upsertBatch(deviceId: DeviceId, locations: List[DeviceLocationEntity]): F[Int] = {
    val sorted = locations.sortWith((x, y) => x.time.isBefore(y.time))

    transactor
      .withTxn {
        debug(deviceId, s"Upserting batch of locations. Size: ${locations.size}") >>
          deviceLocationRepo.upsertBatch(sorted)
      }
      .handleWith[AppError] {
        case _: ViolatesForeignKeyError =>
          transactor
            .withTxn {
              debug(deviceId, "Device not found in the system. Create new before store current location") >>
                createDevice(deviceId) >>
                deviceLocationRepo.upsertBatch(sorted)
            }
        case error => H.raise(error)
      }
  }

  private def createDevice(deviceId: DeviceId): B[Unit] =
    TimeGen[B].genTimeUtc.flatMap { createdAt =>
      deviceRepo.upsert(DeviceEntity(deviceId, DeviceName("Walker"), createdAt)).void
    }

  private def debug(deviceId: DeviceId, message: String) =
    loggerB
      .debug(message)
      .run(LogContext(deviceId = deviceId.some))
      .value

  private def entityToView(value: DeviceLocationEntity): DeviceLocationView =
    value
      .into[DeviceLocationView]
      .withFieldComputed(_.bearing, _.bearing.some)
      .withFieldComputed(_.altitudeAccuracy, _.altitudeAccuracy.some)
      .withFieldComputed(_.time, l => UnixTime(l.time.toInstant(ZoneOffset.UTC).toEpochMilli))
      .transform
}
