package org.tomohavvk.walker.services

import cats.Monad
import cats.effect.kernel.Sync
import cats.syntax.applicative._
import cats.implicits.catsSyntaxFlatMapOps
import cats.implicits.toFlatMapOps
import cats.implicits.toFunctorOps
import cats.mtl.Handle
import cats.mtl.implicits.toHandleOps
import io.odin.Logger
import org.tomohavvk.walker.generation.TimeGen
import org.tomohavvk.walker.persistence.Transactor
import org.tomohavvk.walker.persistence.repository.DeviceGroupRepository
import org.tomohavvk.walker.persistence.repository.GroupRepository
import org.tomohavvk.walker.protocol.Types.CreatedAt
import org.tomohavvk.walker.protocol.Types.DeviceId
import org.tomohavvk.walker.protocol.Types.GroupId
import org.tomohavvk.walker.protocol.Types.UpdatedAt
import org.tomohavvk.walker.protocol.entities.DeviceGroupEntity
import org.tomohavvk.walker.protocol.entities.GroupEntity
import org.tomohavvk.walker.protocol.errors.AppError
import org.tomohavvk.walker.protocol.errors.BadRequestError
import org.tomohavvk.walker.protocol.errors.NotFoundError
import org.tomohavvk.walker.protocol.errors.UniqueConstraintError

trait DevicesGroupService[F[_]] {
  def joinGroup(deviceId: DeviceId, groupId: GroupId): F[DeviceGroupEntity]
}

class DeviceGroupServiceImpl[F[_]: Monad, D[_]: Sync](
  groupRepo:       GroupRepository[D],
  deviceGroupRepo: DeviceGroupRepository[D],
  transactor:      Transactor[F, D],
  loggerF:         Logger[F]
)(implicit HF:     Handle[F, AppError],
  HD:              Handle[D, AppError])
    extends DevicesGroupService[F] {

  override def joinGroup(deviceId: DeviceId, groupId: GroupId): F[DeviceGroupEntity] =
    loggerF.debug("Join group") >>
      transactor
        .withTxn {
          groupRepo
            .findById(groupId)
            .flatMap(validate(deviceId, groupId, _))
            .flatMap(joinDeviceToGroup(deviceId, _))
            .flatTap(incrementCountDeviceInGroup)

        }
        .handleWith[AppError] {
          case _: UniqueConstraintError => HF.raise(BadRequestError("Already joined to the group"))
          case error                    => HF.raise(error)
        }
        .flatTap(_ => loggerF.debug("Join group success"))

  private def validate(deviceId: DeviceId, groupId: GroupId, mbGroup: Option[GroupEntity]): D[GroupEntity] =
    mbGroup match {
      case None                                           => HD.raise(NotFoundError(s"Group: ${groupId.value} not found"))
      case Some(group) if group.ownerDeviceId == deviceId => HD.raise(BadRequestError("Can't join your own group"))
      case Some(group) if !group.isPublic.value           => HD.raise(BadRequestError("Group is private. Access denied"))
      case Some(group)                                    => group.pure[D]
    }

  private def joinDeviceToGroup(deviceId: DeviceId, group: GroupEntity): D[DeviceGroupEntity] =
    TimeGen[D].genTimeUtc
      .map(now => DeviceGroupEntity(deviceId, group.id, CreatedAt(now)))
      .flatMap(deviceGroupRepo.upsert)

  private def incrementCountDeviceInGroup(deviceGroup: DeviceGroupEntity): D[Unit] =
    groupRepo.incrementDeviceCount(deviceGroup.groupId, UpdatedAt(deviceGroup.createdAt.value))
}
