package org.tomohavvk.walker.persistence.repository

import cats.Monad
import cats.implicits.toFunctorOps
import org.tomohavvk.walker.protocol.Types.DeviceId
import org.tomohavvk.walker.protocol.Types.GroupId
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
    fr"""INSERT INTO groups (id, owner_device_id, name, device_count, is_private, created_at, updated_at)
        VALUES ($id, $ownerDeviceId, $name, $deviceCount, $isPrivate, $createdAt, $updatedAt)""".update
  }

  def findByIdQuery(groupId: GroupId): doobie.Query0[GroupEntity] =
    fr"""SELECT id, owner_device_id, name, device_count, is_private, created_at, updated_at FROM groups WHERE id = $groupId"""
      .query[GroupEntity]

  def findAllByDeviceIdQuery(deviceId: DeviceId, limit: Limit, offset: Offset): doobie.Query0[GroupEntity] =
    fr"""select
          groups.id,
          groups.owner_device_id,
          groups.name,
          groups.device_count,
          groups.is_private,
          groups.created_at,
          groups.updated_at
        from
          groups
        left join devices_groups on
          groups.id = devices_groups.group_id
        where groups.owner_device_id = $deviceId or devices_groups.device_id = $deviceId
        limit $limit
        offset $offset"""
      .query[GroupEntity]

  def searchGroupsQuery(
    deviceId: DeviceId,
    search:   Search,
    limit:    Limit,
    offset:   Offset
  ): doobie.Query0[GroupEntity] = {
    val like = s"$search%"
    fr"""select
          groups.id,
          groups.owner_device_id,
          groups.name,
          groups.device_count,
          groups.is_private,
          groups.created_at,
          groups.updated_at
        from
          groups
        left join devices_groups on
          groups.id = devices_groups.group_id
        where
          ((groups.owner_device_id = $deviceId
            or devices_groups.device_id = $deviceId)
          or groups.is_private = false)
          and name ilike $like
          limit $limit
          offset $offset"""
      .query[GroupEntity]
  }

  def incrementDeviceCountQuery(groupId: GroupId, updatedAt: UpdatedAt): doobie.Update0 =
    fr"""UPDATE groups SET device_count = device_count + 1, updated_at = $updatedAt WHERE id = $groupId""".update
}
