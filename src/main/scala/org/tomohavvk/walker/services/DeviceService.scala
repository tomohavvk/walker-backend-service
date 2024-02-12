package org.tomohavvk.walker.services

import cats.data.EitherT
import cats.data.Kleisli
import cats.effect.kernel.Clock
import cats.effect.kernel.Sync
import cats.implicits.toFlatMapOps
import cats.implicits.toFunctorOps
import cats.mtl.Handle
import cats.mtl.implicits.toHandleOps
import io.scalaland.chimney.dsl._
import org.tomohavvk.walker.generation.TimeGen
import org.tomohavvk.walker.persistence.Transactor
import org.tomohavvk.walker.persistence.repository.DeviceRepository
import org.tomohavvk.walker.protocol.Types.DeviceId
import org.tomohavvk.walker.protocol.commands.CreateDeviceCommand
import org.tomohavvk.walker.protocol.entities.DeviceEntity
import org.tomohavvk.walker.protocol.errors.AppError
import org.tomohavvk.walker.protocol.errors.NotFoundError
import org.tomohavvk.walker.protocol.views.DeviceView
import org.tomohavvk.walker.utils.ContextFlow
import org.tomohavvk.walker.utils.liftFSyntax

trait DeviceService[F[_]] {
  def findDevice(deviceId:   DeviceId): ContextFlow[F, DeviceView]
  def createDevice(deviceId: DeviceId, command: CreateDeviceCommand): ContextFlow[F, DeviceView]
}

class DeviceServiceImpl[F[_]: Sync: Clock, B[_]: Sync](
  deviceRepo: DeviceRepository[B],
  transactor: Transactor[F, B]
)(implicit H: Handle[B, Throwable])
    extends DeviceService[F] {

  override def findDevice(deviceId: DeviceId): ContextFlow[F, DeviceView] =
    transactor
      .withTxn {
        deviceRepo.findById(deviceId)
      }
      .liftFlow
      .flatMap {
        case Some(value) => Kleisli.liftF(EitherT.pure[F, AppError](value.transformInto[DeviceView]))
        case None        => Kleisli.liftF(EitherT.leftT[F, DeviceView](NotFoundError(s"Device: ${deviceId.value} not found")))
      }

  // FIXME fix error handling
  override def createDevice(deviceId: DeviceId, command: CreateDeviceCommand): ContextFlow[F, DeviceView] =
    transactor.withTxn {
      TimeGen[B].genTimeUtc.flatMap { createdAt =>
        val entity = DeviceEntity(deviceId, command.name, createdAt)

        deviceRepo
          .upsert(DeviceEntity(deviceId, command.name, createdAt))
          .as(entity.transformInto[DeviceView])
          .handleWith[Throwable] { error =>
            println(2222)
            H.raise(error)
          }
      }
    }.liftFlow

}
