package org.tomohavvk.walker.services

import cats.Monad
import cats.effect.kernel.Sync
import cats.implicits.catsSyntaxFlatMapOps
import cats.implicits.toFlatMapOps
import cats.implicits.toFunctorOps
import cats.mtl.Handle
import cats.mtl.implicits.toHandleOps
import io.odin.Logger
import org.tomohavvk.walker.generation.NanoIdGen
import org.tomohavvk.walker.generation.TimeGen
import org.tomohavvk.walker.persistence.Transactor
import org.tomohavvk.walker.persistence.repository.GroupRepository
import org.tomohavvk.walker.protocol.Types.CreatedAt
import org.tomohavvk.walker.protocol.Types.DeviceCount
import org.tomohavvk.walker.protocol.Types.DeviceId
import org.tomohavvk.walker.protocol.Types.GroupId
import org.tomohavvk.walker.protocol.Types.GroupName
import org.tomohavvk.walker.protocol.Types.IsPrivate
import org.tomohavvk.walker.protocol.Types.Limit
import org.tomohavvk.walker.protocol.Types.Offset
import org.tomohavvk.walker.protocol.Types.Search
import org.tomohavvk.walker.protocol.Types.UpdatedAt
import org.tomohavvk.walker.protocol.entities.GroupEntity
import org.tomohavvk.walker.protocol.errors.AppError
import org.tomohavvk.walker.protocol.errors.NotFoundError
import org.tomohavvk.walker.protocol.errors.UniqueConstraintError
import org.tomohavvk.walker.protocol.errors.ViolatesForeignKeyError

trait GroupService[F[_]] {
  def createGroup(deviceId:                     DeviceId, name:   GroupName, isPrivate: IsPrivate): F[GroupEntity]
  def getAllDeviceOwnedOrJoinedGroups(deviceId: DeviceId, limit:  Limit, offset:        Offset): F[List[GroupEntity]]
  def searchGroups(deviceId:                    DeviceId, search: Search, limit:        Limit, offset: Offset): F[List[GroupEntity]]
}

class GroupServiceImpl[F[_]: Monad, D[_]: Sync](
  groupRepo:   GroupRepository[D],
  transactor:  Transactor[F, D],
  loggerF:     Logger[F]
)(implicit HF: Handle[F, AppError])
    extends GroupService[F] {

  override def createGroup(deviceId: DeviceId, name: GroupName, isPrivate: IsPrivate): F[GroupEntity] =
    loggerF.debug("Create group") >>
      transactor
        .withTxn(makeEntity(deviceId, name, isPrivate).flatMap(groupRepo.upsert))
        .handleWith[AppError] {
          case error: UniqueConstraintError => HF.raise(error.copy(message = "Group with same name already exists"))
          case _: ViolatesForeignKeyError   => HF.raise(NotFoundError(s"Device: ${deviceId.value} not found"))
          case error                        => HF.raise(error)
        }
        .flatTap(_ => loggerF.debug("Create group success"))

  override def getAllDeviceOwnedOrJoinedGroups(deviceId: DeviceId, limit: Limit, offset: Offset): F[List[GroupEntity]] =
    loggerF.debug("Get all device owned or joined groups") >>
      transactor
        .withTxn(groupRepo.findAllByDeviceId(deviceId, limit, offset))
        .flatTap(_ => loggerF.debug("Get all device owned or joined groups success"))

  override def searchGroups(deviceId: DeviceId, search: Search, limit: Limit, offset: Offset): F[List[GroupEntity]] =
    loggerF.debug("Search groups") >>
      transactor
        .withTxn(groupRepo.searchGroups(deviceId, search, limit, offset))
        .flatTap(_ => loggerF.debug("Get all device owned or joined groups success"))

  private def makeEntity(deviceId: DeviceId, name: GroupName, isPrivate: IsPrivate): D[GroupEntity] =
    TimeGen[D].genTimeUtc.flatMap { createdAt =>
      NanoIdGen[D].randomNanoId.map { nanoId =>
        GroupEntity(
          id = GroupId(nanoId),
          ownerDeviceId = deviceId,
          name = name,
          deviceCount = DeviceCount(1),
          isPrivate = isPrivate,
          createdAt = CreatedAt(createdAt),
          updatedAt = UpdatedAt(createdAt)
        )

      }
    }
}
