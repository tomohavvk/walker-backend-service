package org.tomohavvk.walker.services

import cats.effect.kernel.Clock
import cats.effect.kernel.Sync
import cats.implicits.catsSyntaxFlatMapOps
import cats.implicits.toFlatMapOps
import cats.implicits.toFunctorOps
import cats.mtl.Handle
import cats.mtl.implicits.toHandleOps
import io.odin.Logger
import org.tomohavvk.walker.generation.TimeGen
import org.tomohavvk.walker.persistence.Transactor
import org.tomohavvk.walker.persistence.repository.DeviceLocationRepository
import org.tomohavvk.walker.persistence.repository.DeviceRepository
import org.tomohavvk.walker.protocol.Types.DeviceId
import org.tomohavvk.walker.protocol.Types.DeviceName
import org.tomohavvk.walker.protocol.entities.DeviceEntity
import org.tomohavvk.walker.protocol.entities.DeviceLocationEntity
import org.tomohavvk.walker.protocol.entities.DeviceLocationEntity.DeviceLocationEntityExt
import org.tomohavvk.walker.protocol.errors.AppError
import org.tomohavvk.walker.protocol.errors.NotFoundError
import org.tomohavvk.walker.protocol.errors.ViolatesForeignKeyError
import org.tomohavvk.walker.protocol.views.DeviceLocationView

trait LocationService[F[_]] {
  def lastLocation(deviceId: DeviceId): F[DeviceLocationView]
  def upsertBatch(deviceId:  DeviceId, locations: List[DeviceLocationEntity]): F[Int]
}

class LocationServiceImpl[F[_]: Sync: Clock, D[_]: Sync](
  deviceRepo:         DeviceRepository[D],
  deviceLocationRepo: DeviceLocationRepository[D],
  transactor:         Transactor[F, D],
  loggerF:            Logger[F],
  loggerD:            Logger[D]
)(implicit HF:        Handle[F, AppError])
    extends LocationService[F] {

  override def lastLocation(deviceId: DeviceId): F[DeviceLocationView] =
    loggerF.debug("Device last location request") >>
      transactor
        .withTxn(deviceLocationRepo.findLastById(deviceId))
        .flatMap {
          case Some(value) => HF.applicative.pure(value.asView)
          case None        => HF.raise(NotFoundError(s"Device: ${deviceId.value} not exists in the system"))
        }

  override def upsertBatch(deviceId: DeviceId, locations: List[DeviceLocationEntity]): F[Int] = {
    val sorted = locations.sortWith((x, y) => x.time.isBefore(y.time))
    debug(deviceId, s"Upserting batch of locations. Size: ${locations.size}") >>
      transactor
        .withTxn(deviceLocationRepo.upsertBatch(sorted))
        .handleWith[AppError] {
          case _: ViolatesForeignKeyError =>
            debug(deviceId, "Device not found in the system. Create new before store current location") >>
              transactor.withTxn(createDevice(deviceId) >> deviceLocationRepo.upsertBatch(sorted))
          case error => HF.raise(error)
        }
  }

  private def createDevice(deviceId: DeviceId): D[Unit] =
    TimeGen[D].genTimeUtc.flatMap { createdAt =>
      deviceRepo.upsert(DeviceEntity(deviceId, DeviceName("Walker"), createdAt)).void
    }

  private def debug(deviceId: DeviceId, message: String): F[Unit] =
    loggerF
      .debug(s"|${deviceId.value}| $message")

}
