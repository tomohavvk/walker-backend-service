package org.tomohavvk.walker.services

import cats.Monad
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
import org.tomohavvk.walker.protocol.Types.CreatedAt
import org.tomohavvk.walker.protocol.Types.DeviceCount
import org.tomohavvk.walker.protocol.Types.DeviceId
import org.tomohavvk.walker.protocol.Types.GroupId
import org.tomohavvk.walker.protocol.Types.UpdatedAt
import org.tomohavvk.walker.protocol.commands.CreateGroupCommand
import org.tomohavvk.walker.protocol.entities.GroupEntity
import org.tomohavvk.walker.protocol.errors.AppError
import org.tomohavvk.walker.protocol.errors.NotFoundError
import org.tomohavvk.walker.protocol.errors.UniqueConstraintError
import org.tomohavvk.walker.protocol.errors.ViolatesForeignKeyError
import org.tomohavvk.walker.protocol.views.GroupView

trait GroupService[F[_]] {
  def createGroup(deviceId:                     DeviceId, command: CreateGroupCommand): F[GroupView]
  def getAllDeviceOwnedOrJoinedGroups(deviceId: DeviceId): F[List[GroupView]]
}

class GroupServiceImpl[F[_]: Monad, D[_]: Sync](
  groupRepo:   GroupRepository[D],
  transactor:  Transactor[F, D],
  loggerF:     Logger[F]
)(implicit HF: Handle[F, AppError],
  HD:          Handle[D, AppError])
    extends GroupService[F] {

  override def createGroup(deviceId: DeviceId, command: CreateGroupCommand): F[GroupView] =
    loggerF.debug("Create group request") >>
      transactor
        .withTxn(makeEntity(deviceId, command).flatMap(groupRepo.upsert).map(_.transformInto[GroupView]))
        .handleWith[AppError] {
          case error: UniqueConstraintError => HF.raise(error.copy(message = "Group with same name already exists"))
          case _: ViolatesForeignKeyError   => HF.raise(NotFoundError(s"Device: ${deviceId.value} not found"))
          case error                        => HF.raise(error)
        }
        .flatTap(_ => loggerF.debug("Create group success"))

  override def getAllDeviceOwnedOrJoinedGroups(deviceId: DeviceId): F[List[GroupView]] =
    loggerF.debug("Get all device owned or joined groups request") >>
      transactor
        .withTxn(groupRepo.findAllByDeviceId(deviceId))
        .map(_.map(_.transformInto[GroupView]))
        .flatTap(_ => loggerF.debug("Get all device owned or joined groups success"))

  private def makeEntity(deviceId: DeviceId, command: CreateGroupCommand): D[GroupEntity] =
    TimeGen[D].genTimeUtc.flatMap { createdAt =>
      NanoIdGen[D].randomNanoId.map { nanoId =>
        command
          .into[GroupEntity]
          .withFieldConst(_.id, GroupId(nanoId))
          .withFieldConst(_.ownerDeviceId, deviceId)
          .withFieldConst(_.deviceCount, DeviceCount(1))
          .withFieldConst(_.createdAt, CreatedAt(createdAt))
          .withFieldConst(_.updatedAt, UpdatedAt(createdAt))
          .transform
      }
    }
}
