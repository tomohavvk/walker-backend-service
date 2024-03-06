package org.tomohavvk.walker.services

import cats.Monad
import cats.syntax.applicative._
import cats.implicits.catsSyntaxFlatMapOps
import cats.implicits.toFlatMapOps
import cats.mtl.Handle
import cats.mtl.implicits.toHandleOps
import io.odin.Logger
import org.tomohavvk.walker.generation.TimeGen
import org.tomohavvk.walker.persistence.Transactor
import org.tomohavvk.walker.persistence.repository.DeviceRepository
import org.tomohavvk.walker.protocol.Types.CreatedAt
import org.tomohavvk.walker.protocol.Types.DeviceId
import org.tomohavvk.walker.protocol.commands.RegisterDeviceCommand
import org.tomohavvk.walker.protocol.entities.DeviceEntity
import org.tomohavvk.walker.protocol.errors.AppError
import org.tomohavvk.walker.protocol.errors.NotFoundError
import org.tomohavvk.walker.protocol.errors.UniqueConstraintError

trait DeviceService[F[_]] {
  def getDevice(deviceId: DeviceId): F[DeviceEntity]
  def register(deviceId:  DeviceId, command: RegisterDeviceCommand): F[DeviceEntity]
}

class DeviceServiceImpl[F[_]: Monad, D[_]](
  deviceRepo:  DeviceRepository[D],
  transactor:  Transactor[F, D],
  loggerF:     Logger[F]
)(implicit HE: Handle[F, AppError],
  T:           TimeGen[F])
    extends DeviceService[F] {

  override def getDevice(deviceId: DeviceId): F[DeviceEntity] =
    loggerF.debug("Get device") >>
      transactor
        .withTxn(deviceRepo.findById(deviceId))
        .flatMap {
          case Some(device) => device.pure[F]
          case None         => HE.raise(NotFoundError(s"Device: ${deviceId.value} not found"))
        }

  override def register(deviceId: DeviceId, command: RegisterDeviceCommand): F[DeviceEntity] =
    loggerF.debug("Register device") >>
      TimeGen[F].genTimeUtc
        .flatMap { createdAt =>
          transactor
            .withTxn {
              deviceRepo.upsert(DeviceEntity(deviceId, command.name, CreatedAt(createdAt)))
            }
        }
        .handleWith[AppError] {
          case error: UniqueConstraintError =>
            HE.raise(error.copy(message = s"Device with same device id: ${deviceId.value} already registered"))
          case error => HE.raise(error)
        }
}
