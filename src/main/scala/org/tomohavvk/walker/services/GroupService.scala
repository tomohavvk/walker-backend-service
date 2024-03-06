package org.tomohavvk.walker.services

import cats.implicits.catsSyntaxFlatMapOps
import cats.implicits.toFlatMapOps
import cats.implicits.toFunctorOps
import cats.mtl.implicits.toHandleOps
import cats.Applicative
import cats.Monad
import cats.Monoid
import cats.mtl.Handle
import io.odin.Logger
import org.tomohavvk.walker.generation.TimeGen
import org.tomohavvk.walker.persistence.Transactor
import org.tomohavvk.walker.persistence.repository.DeviceGroupRepository
import org.tomohavvk.walker.persistence.repository.GroupRepository
import org.tomohavvk.walker.protocol.Types._
import org.tomohavvk.walker.protocol.entities.DeviceGroupEntity
import org.tomohavvk.walker.protocol.entities.GroupEntity
import org.tomohavvk.walker.protocol.errors._
import org.tomohavvk.walker.protocol.ws.GroupCreate

trait GroupService[F[_]] {
  def createGroup(deviceId:                     DeviceId, create: GroupCreate): F[GroupEntity]
  def getAllDeviceOwnedOrJoinedGroups(deviceId: DeviceId, limit:  Limit, offset: Offset): F[List[GroupEntity]]
  def isPublicIdAvailable(publicId:             GroupPublicId): F[IsPublicIdAvailable]
  def searchGroups(deviceId:                    DeviceId, search: Search, limit: Limit, offset: Offset): F[List[GroupEntity]]
}

class GroupServiceImpl[F[_]: Monad, D[_]: Monad](
  groupRepo:       GroupRepository[D],
  deviceGroupRepo: DeviceGroupRepository[D],
  transactor:      Transactor[F, D],
  loggerF:         Logger[F]
)(implicit HE:     Handle[F, AppError],
  T:               TimeGen[F])
    extends GroupService[F] {

  override def createGroup(deviceId: DeviceId, create: GroupCreate): F[GroupEntity] =
    loggerF.debug("Create group") >>
      check(create) >>
      makeGroupEntity(deviceId, create)
        .flatTap { groupEntity =>
          transactor
            .withTxn {
              val deviceGroupEntity = DeviceGroupEntity(deviceId, groupEntity.id, groupEntity.createdAt)

              groupRepo.upsert(groupEntity) >>
                deviceGroupRepo.upsert(deviceGroupEntity)
            }
        }
        .handleWith[AppError] {
          case _: ViolatesForeignKeyError => HE.raise(NotFoundError(s"Device: ${deviceId.value} not found"))
          case error                      => HE.raise(error)
        }
        .flatTap(_ => loggerF.debug("Create group success"))
        .map(_.copy(isJoined = IsJoined(true)))

  override def isPublicIdAvailable(publicId: GroupPublicId): F[IsPublicIdAvailable] =
    loggerF.debug(s"Check public id availability: ${publicId.value}") >>
      transactor
        .withTxn(groupRepo.isPublicIdExists(publicId))
        .map(isExist => IsPublicIdAvailable(!isExist))
        .flatTap(isAvailable => loggerF.debug(s"Public id:  ${publicId.value} availability: $isAvailable"))

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

  private def check(create: GroupCreate): F[Unit] =
    Applicative[F].whenA(create.isPublic.value && create.publicId.exists(_.value.isEmpty)) {
      HE.raise(BadRequestError(s"Public ID should not be empty for public group"))
    }

  private def makeGroupEntity(deviceId: DeviceId, create: GroupCreate): F[GroupEntity] =
    TimeGen[F].genTimeUtc.map { createdAt =>
      val publicId = create.publicId.getOrElse(GroupPublicId(create.id.value))

      GroupEntity(
        id = create.id,
        publicId = publicId,
        ownerDeviceId = deviceId,
        name = create.name,
        description = create.description.getOrElse(Description(Monoid[String].empty)),
        deviceCount = DeviceCount(1),
        isPublic = create.isPublic,
        createdAt = CreatedAt(createdAt),
        updatedAt = UpdatedAt(createdAt)
      )
    }
}
