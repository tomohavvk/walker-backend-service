package org.tomohavvk.walker.services

import cats.effect.kernel.Clock
import cats.effect.kernel.Sync
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
import org.tomohavvk.walker.protocol.commands.CreateDeviceCommand
import org.tomohavvk.walker.protocol.entities.DeviceEntity
import org.tomohavvk.walker.protocol.errors.AppError
import org.tomohavvk.walker.protocol.errors.NotFoundError
import org.tomohavvk.walker.protocol.views.DeviceView

trait DeviceService[F[_]] {
  def findDevice(deviceId:   DeviceId): F[DeviceView]
  def createDevice(deviceId: DeviceId, command: CreateDeviceCommand): F[DeviceView]
}

class DeviceServiceImpl[F[_]: Sync: Clock, D[_]: Sync](
  deviceRepo:  DeviceRepository[D],
  transactor:  Transactor[F, D],
  loggerF:     Logger[F]
)(implicit HF: Handle[F, AppError],
  HD:          Handle[D, AppError])
    extends DeviceService[F] {

  override def findDevice(deviceId: DeviceId): F[DeviceView] =
    loggerF.debug("Find device request") >>
      transactor
        .withTxn(deviceRepo.findById(deviceId))
        .flatMap {
          case Some(value) => HF.applicative.pure(value.transformInto[DeviceView])
          case None        => HF.raise(NotFoundError(s"Device: ${deviceId.value} not found"))
        }

  override def createDevice(deviceId: DeviceId, command: CreateDeviceCommand): F[DeviceView] =
    loggerF.debug("Create device request") >>
      transactor.withTxn {
        TimeGen[D].genTimeUtc.flatMap { createdAt =>
          val entity = DeviceEntity(deviceId, command.name, CreatedAt(createdAt))

          deviceRepo
            .upsert(entity)
            .as(entity.transformInto[DeviceView])
            .handleWith[AppError](error => HD.raise(error))
        }
      }
}
