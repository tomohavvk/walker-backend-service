package org.tomohavvk.walker.services

import cats.Monad
import cats.effect.kernel.Sync
import cats.implicits.catsSyntaxFlatMapOps
import cats.implicits.toFlatMapOps
import cats.implicits.toFunctorOps
import cats.mtl.Handle
import io.odin.Logger
import io.scalaland.chimney.dsl._
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
import org.tomohavvk.walker.protocol.views.DeviceGroupView

trait DevicesGroupService[F[_]] {
  def joinGroup(deviceId: DeviceId, groupId: GroupId): F[DeviceGroupView]
}

class DevicesGroupServiceImpl[F[_]: Monad, D[_]: Sync](
  groupRepo:       GroupRepository[D],
  deviceGroupRepo: DeviceGroupRepository[D],
  deviceService:   DeviceService[F],
  transactor:      Transactor[F, D],
  loggerF:         Logger[F]
)(implicit HF:     Handle[F, AppError],
  HD:              Handle[D, AppError])
    extends DevicesGroupService[F] {

  // TODO decompose
  override def joinGroup(deviceId: DeviceId, groupId: GroupId): F[DeviceGroupView] =
    loggerF.debug("Join group request") >>
      transactor
        .withTxn {
          groupRepo
            .findById(groupId)
            .flatMap[GroupEntity] {
              case None => HD.raise(NotFoundError(s"Group: ${groupId.value} not found"))
              case Some(group) if group.ownerDeviceId == deviceId =>
                HD.raise(BadRequestError("Can't join your own group"))
              case Some(group) if group.isPrivate.value => HD.raise(BadRequestError("Group is private. Access denied"))
              case Some(group)                          => HD.applicative.pure(group)
            }
            .flatMap(group => deviceGroupRepo.findById(deviceId, groupId).map(x => group -> x))
            .flatMap[GroupEntity] {
              case (_, Some(_))  => HD.raise(BadRequestError("You already joined to the group"))
              case (group, None) => HD.applicative.pure(group)
            }
            .flatMap { group =>
              TimeGen[D].genTimeUtc.flatMap { now =>
                val entity = DeviceGroupEntity(deviceId, group.id, CreatedAt(now))
                for {
                  _ <- deviceGroupRepo.upsert(entity)
                  _ <- groupRepo.incrementDeviceCounter(group.id, UpdatedAt(now))
                } yield entity.transformInto[DeviceGroupView]
              }
            }
        }
}
