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
import org.tomohavvk.walker.generation.NanoIdGen
import org.tomohavvk.walker.generation.TimeGen
import org.tomohavvk.walker.persistence.Transactor
import org.tomohavvk.walker.persistence.repository.GroupRepository
import org.tomohavvk.walker.protocol.Types.DeviceCount
import org.tomohavvk.walker.protocol.Types.GroupId
import org.tomohavvk.walker.protocol.commands.CreateGroupCommand
import org.tomohavvk.walker.protocol.entities.GroupEntity
import org.tomohavvk.walker.protocol.errors.AppError
import org.tomohavvk.walker.protocol.errors.UniqueConstraintError
import org.tomohavvk.walker.protocol.views.GroupView

trait GroupService[F[_]] {
  def createGroup(command: CreateGroupCommand): F[GroupView]
}

class GroupServiceImpl[F[_]: Sync: Clock, D[_]: Sync](
  groupRepo:     GroupRepository[D],
  deviceService: DeviceService[F],
  transactor:    Transactor[F, D],
  loggerF:       Logger[F]
)(implicit HF:   Handle[F, AppError],
  HD:            Handle[D, AppError])
    extends GroupService[F] {

  override def createGroup(command: CreateGroupCommand): F[GroupView] =
    loggerF.debug("Create group request") >>
      deviceService.findDevice(command.ownerDeviceId) >>
      transactor
        .withTxn {

          NanoIdGen[D].randomNanoId.flatMap { nanoId =>
            TimeGen[D].genTimeUtc.flatMap { createdAt =>
              val entity = GroupEntity(GroupId(nanoId), command.name, DeviceCount(1), command.ownerDeviceId, createdAt)

              groupRepo
                .upsert(entity)
                .as(entity.transformInto[GroupView])
                .handleWith[AppError](error => HD.raise(error))
            }
          }
        }
        .handleWith[AppError] {
          case error: UniqueConstraintError => HF.raise(error.copy(message = "Group with same name already exists"))
          case error                        => HF.raise(error)
        }
}
