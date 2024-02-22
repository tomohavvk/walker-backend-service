package org.tomohavvk.walker.persistence.repository

import cats.Monad
import cats.implicits.toFunctorOps
import org.tomohavvk.walker.protocol.Types.DeviceId
import org.tomohavvk.walker.protocol.Types.GroupId
import org.tomohavvk.walker.protocol.Types.GroupPublicId
import org.tomohavvk.walker.protocol.Types.Limit
import org.tomohavvk.walker.protocol.Types.Offset
import org.tomohavvk.walker.protocol.Types.Search
import org.tomohavvk.walker.protocol.Types.UpdatedAt
import org.tomohavvk.walker.protocol.entities.GroupEntity
import doobie.implicits._
import org.tomohavvk.walker.persistence._
import org.tomohavvk.walker.protocol.errors.AppError
import org.tomohavvk.walker.utils.LiftConnectionIO

trait GroupRepository[D[_]] {
  def upsert(entity: GroupEntity): D[GroupEntity]

  def findById(groupId: GroupId): D[Option[GroupEntity]]

  def isPublicIdExists(publicId: GroupPublicId): D[Boolean]

  def findAllByDeviceId(deviceId: DeviceId, limit: Limit, offset: Offset): D[List[GroupEntity]]

  def searchGroups(deviceId: DeviceId, search: Search, limit: Limit, offset: Offset): D[List[GroupEntity]]

  def incrementDeviceCount(groupId: GroupId, updatedAt: UpdatedAt): D[Unit]
}

class DoobieGroupRepository[D[_]: Monad](implicit D: LiftConnectionIO[D, AppError])
    extends GroupRepository[D]
    with GroupQueries {

  override def upsert(entity: GroupEntity): D[GroupEntity] =
    D.lift(upsertQuery(entity).run).as(entity)

  override def findById(groupId: GroupId): D[Option[GroupEntity]] =
    D.lift(findByIdQuery(groupId).option)

  override def isPublicIdExists(publicId: GroupPublicId): D[Boolean] =
    D.lift(isPublicIdExistsQuery(publicId).unique)

  override def findAllByDeviceId(deviceId: DeviceId, limit: Limit, offset: Offset): D[List[GroupEntity]] =
    D.lift(findAllByDeviceIdQuery(deviceId, limit, offset).to[List])

  override def searchGroups(deviceId: DeviceId, search: Search, limit: Limit, offset: Offset): D[List[GroupEntity]] =
    D.lift(searchGroupsQuery(deviceId, search, limit, offset).to[List])

  override def incrementDeviceCount(groupId: GroupId, updatedAt: UpdatedAt): D[Unit] =
    D.lift(incrementDeviceCountQuery(groupId, updatedAt).run.void)
}

trait GroupQueries extends DoobieMeta {

  def upsertQuery(entity: GroupEntity): doobie.Update0 = {
    import entity._
    fr"""insert INTO groups (id, public_id, owner_device_id, name, description, device_count, is_public, created_at, updated_at)
        values ($id, $publicId, $ownerDeviceId, $name, $description, $deviceCount, $isPublic, $createdAt, $updatedAt)""".update
  }

  def findByIdQuery(groupId: GroupId): doobie.Query0[GroupEntity] =
    fr"""select id, public_id, owner_device_id, name, description, device_count, is_public, created_at, updated_at, null as is_joined FROM groups where id = $groupId limit 1"""
      .query[GroupEntity]

  def isPublicIdExistsQuery(publicId: GroupPublicId): doobie.Query0[Boolean] =
    fr"""select (count(public_id) > 0) as exists from groups where public_id = $publicId"""
      .query[Boolean]

  def findAllByDeviceIdQuery(deviceId: DeviceId, limit: Limit, offset: Offset): doobie.Query0[GroupEntity] =
    fr"""select
           groups.id,
           groups.public_id,
           groups.owner_device_id,
           groups.name,
           groups.description,
           groups.device_count,
           groups.is_public,
           groups.created_at,
           groups.updated_at,
           true as is_joined
         from
           groups
         join devices_groups on
           groups.id = devices_groups.group_id
         where
           devices_groups.device_id = $deviceId
          order by
                updated_at desc
         limit $limit offset $offset"""
      .query[GroupEntity]

  def searchGroupsQuery(
    deviceId: DeviceId,
    search:   Search,
    limit:    Limit,
    offset:   Offset
  ): doobie.Query0[GroupEntity] = {

    // FIXME add validation and do not search less then 4 symbols to avoid indexed large ram allocation?
    val nameLike     = if (search.value.length < 3) s"$search%" else s"%$search%"
    val publicIdLike = s"$search%"

    fr"""select
           groups.id,
           groups.public_id,
           groups.owner_device_id,
           groups.name,
           groups.description,
           groups.device_count,
           groups.is_public,
           groups.created_at,
           groups.updated_at,
           case
             when devices_groups.group_id is not null then true
             else false
           end as is_joined
         from
           groups
         left join devices_groups on
           groups.id = devices_groups.group_id
           and devices_groups.device_id = $deviceId
         where
           groups.is_public = true
           and (name ilike $nameLike or public_id ilike $publicIdLike)
         order by
           updated_at desc
         limit $limit offset $offset"""
      .query[GroupEntity]
  }

  def incrementDeviceCountQuery(groupId: GroupId, updatedAt: UpdatedAt): doobie.Update0 =
    fr"""UPDATE groups SET device_count = device_count + 1, updated_at = $updatedAt WHERE id = $groupId""".update
}
