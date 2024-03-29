package org.tomohavvk.walker.services

import cats.Monad
import cats.implicits.catsSyntaxFlatMapOps
import cats.implicits.toFlatMapOps
import cats.implicits.toFunctorOps
import cats.mtl.Handle
import cats.mtl.implicits.toHandleOps
import cats.syntax.applicative._
import io.odin.Logger
import io.scalaland.chimney.dsl.TransformerOps
import org.tomohavvk.walker.generation.TimeGen
import org.tomohavvk.walker.persistence.Transactor
import org.tomohavvk.walker.persistence.repository.DeviceLocationRepository
import org.tomohavvk.walker.persistence.repository.DeviceRepository
import org.tomohavvk.walker.protocol.DeviceLocation
import org.tomohavvk.walker.protocol.Types.AltitudeAccuracy
import org.tomohavvk.walker.protocol.Types.Bearing
import org.tomohavvk.walker.protocol.Types.CreatedAt
import org.tomohavvk.walker.protocol.Types.DeviceId
import org.tomohavvk.walker.protocol.Types.DeviceName
import org.tomohavvk.walker.protocol.entities.DeviceEntity
import org.tomohavvk.walker.protocol.entities.DeviceLocationEntity
import org.tomohavvk.walker.protocol.errors.AppError
import org.tomohavvk.walker.protocol.errors.NotFoundError
import org.tomohavvk.walker.protocol.errors.ViolatesForeignKeyError

import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset

trait LocationService[F[_]] {
  def lastLocation(deviceId: DeviceId): F[DeviceLocationEntity]
  def upsertBatch(deviceId:  DeviceId, locations: List[DeviceLocation]): F[Int]
}

class LocationServiceImpl[F[_]: Monad, D[_]: Monad](
  deviceRepo:         DeviceRepository[D],
  deviceLocationRepo: DeviceLocationRepository[D],
  transactor:         Transactor[F, D],
  loggerF:            Logger[F]
)(implicit
  HE: Handle[F, AppError],
  T:  TimeGen[F])
    extends LocationService[F] {

  override def lastLocation(deviceId: DeviceId): F[DeviceLocationEntity] =
    loggerF.debug("Device last location") >>
      transactor
        .withTxn(deviceLocationRepo.findLastById(deviceId))
        .flatMap {
          case Some(location) => location.pure[F]
          case None           => HE.raise(NotFoundError(s"Device: ${deviceId.value} last location not found in the system"))
        }

  override def upsertBatch(deviceId: DeviceId, locations: List[DeviceLocation]): F[Int] = {
    val entities     = makeEntities(deviceId, locations)
    val sortedByTime = entities.sortWith((x, y) => x.time.isBefore(y.time))

    debug(deviceId, s"Upserting batch of locations. Size: ${locations.size}") >>
      TimeGen[F].genTimeUtc.flatMap { now =>
        transactor
          .withTxn(deviceLocationRepo.upsertBatch(sortedByTime))
          .handleWith[AppError] {
            case _: ViolatesForeignKeyError =>
              debug(deviceId, "Device not found in the system. Create new before store current location") >>
                transactor.withTxn(
                  createDevice(deviceId, CreatedAt(now)) >> deviceLocationRepo.upsertBatch(sortedByTime)
                )
            case error => HE.raise(error)
          }
      }
  }

  private def createDevice(deviceId: DeviceId, createdAt: CreatedAt): D[Unit] =
    deviceRepo.upsert(DeviceEntity(deviceId, DeviceName("Walker"), createdAt)).void

  private def debug(deviceId: DeviceId, message: String): F[Unit] =
    loggerF
      .debug(s"|${deviceId.value}| $message")

  private def makeEntities(deviceId: DeviceId, locations: List[DeviceLocation]): List[DeviceLocationEntity] =
    locations
      .map { location =>
        location
          .into[DeviceLocationEntity]
          .withFieldConst(_.deviceId, deviceId)
          .withFieldComputed(_.bearing, _.bearing.getOrElse(Bearing(0)))
          .withFieldComputed(_.altitudeAccuracy, _.altitudeAccuracy.getOrElse(AltitudeAccuracy(0)))
          .withFieldComputed(_.time, l => LocalDateTime.ofInstant(Instant.ofEpochMilli(l.time.value), ZoneOffset.UTC))
          .transform
      }
}
