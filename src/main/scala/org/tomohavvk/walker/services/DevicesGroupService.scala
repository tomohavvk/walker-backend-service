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

class DeviceGroupServiceImpl[F[_]: Monad, D[_]: Monad](
  groupRepo:       GroupRepository[D],
  deviceGroupRepo: DeviceGroupRepository[D],
  transactor:      Transactor[F, D],
  loggerF:         Logger[F]
)(implicit
  HD: Handle[D, AppError],
  HE: Handle[F, AppError],
  T:  TimeGen[F])
    extends DevicesGroupService[F] {

  override def joinGroup(deviceId: DeviceId, groupId: GroupId): F[DeviceGroupEntity] =
    loggerF.debug("Join group") >>
      TimeGen[F].genTimeUtc
        .flatMap { now =>
          transactor
            .withTxn {
              groupRepo
                .findById(groupId)
                .flatMap(validate(deviceId, groupId, _))
                .flatMap(joinDeviceToGroup(deviceId, _, CreatedAt(now)))
                .flatTap(incrementCountDeviceInGroup)

            }
        }
        .handleWith[AppError] {
          case _: UniqueConstraintError => HE.raise(BadRequestError("Already joined to the group"))
          case error                    => HE.raise(error)
        }
        .flatTap(_ => loggerF.debug("Join group success"))

  private def validate(deviceId: DeviceId, groupId: GroupId, mbGroup: Option[GroupEntity]): D[GroupEntity] =
    mbGroup match {
      case None                                           => HD.raise(NotFoundError(s"Group: ${groupId.value} not found"))
      case Some(group) if group.ownerDeviceId == deviceId => HD.raise(BadRequestError("Can't join your own group"))
      case Some(group) if !group.isPublic.value           => HD.raise(BadRequestError("Group is private. Access denied"))
      case Some(group)                                    => group.pure[D]
    }

  private def joinDeviceToGroup(deviceId: DeviceId, group: GroupEntity, createdAt: CreatedAt): D[DeviceGroupEntity] =
    deviceGroupRepo.upsert(DeviceGroupEntity(deviceId, group.id, createdAt))

  private def incrementCountDeviceInGroup(deviceGroup: DeviceGroupEntity): D[Unit] =
    groupRepo.incrementDeviceCount(deviceGroup.groupId, UpdatedAt(deviceGroup.createdAt.value))
}
