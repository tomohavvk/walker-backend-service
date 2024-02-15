package org.tomohavvk.walker.services

import cats.effect.kernel.Clock
import cats.effect.kernel.Sync
import cats.syntax.applicative._
import cats.implicits.catsSyntaxFlatMapOps
import cats.implicits.toFlatMapOps
import cats.implicits.toFunctorOps
import cats.mtl.Handle
import cats.mtl.implicits.toHandleOps
import io.odin.Logger
import io.scalaland.chimney.dsl._
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
import org.tomohavvk.walker.protocol.views.DeviceView

trait DeviceService[F[_]] {
  def getDevice(deviceId: DeviceId): F[DeviceView]
  def register(deviceId:  DeviceId, command: RegisterDeviceCommand): F[DeviceView]
}

class DeviceServiceImpl[F[_]: Sync: Clock, D[_]: Sync](
  deviceRepo:  DeviceRepository[D],
  transactor:  Transactor[F, D],
  loggerF:     Logger[F]
)(implicit HF: Handle[F, AppError],
  HD:          Handle[D, AppError])
    extends DeviceService[F] {

  override def getDevice(deviceId: DeviceId): F[DeviceView] =
    loggerF.debug("Get device request") >>
      transactor
        .withTxn(deviceRepo.findById(deviceId))
        .flatMap {
          case Some(device) => device.transformInto[DeviceView].pure[F]
          case None         => HF.raise(NotFoundError(s"Device: ${deviceId.value} not found"))
        }

  override def register(deviceId: DeviceId, command: RegisterDeviceCommand): F[DeviceView] =
    loggerF.debug("Register device request") >>
      transactor
        .withTxn {
          TimeGen[D].genTimeUtc.flatMap { createdAt =>
            deviceRepo
              .upsert {
                DeviceEntity(deviceId, command.name, CreatedAt(createdAt))
              }
              .map(_.transformInto[DeviceView])
          }
        }
        .handleWith[AppError] {
          case error: UniqueConstraintError =>
            HF.raise(error.copy(message = "Device with same device_id already registered"))
          case error => HF.raise(error)
        }
}
