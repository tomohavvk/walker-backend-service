package org.tomohavvk.walker.persistence.repository

import cats.Monad
import cats.implicits.toFunctorOps
import org.tomohavvk.walker.protocol.Types.GroupId
import org.tomohavvk.walker.protocol.Types.UpdatedAt
import org.tomohavvk.walker.protocol.entities.GroupEntity
import doobie.implicits._
import org.tomohavvk.walker.persistence._
import org.tomohavvk.walker.protocol.errors.AppError
import org.tomohavvk.walker.utils.LiftConnectionIO

trait GroupRepository[D[_]] {
  def upsert(entity:                GroupEntity): D[GroupEntity]
  def findById(groupId:             GroupId): D[Option[GroupEntity]]
  def incrementDeviceCount(groupId: GroupId, updatedAt: UpdatedAt): D[Unit]
}

class DoobieGroupRepository[D[_]: Monad](implicit D: LiftConnectionIO[D, AppError])
    extends GroupRepository[D]
    with GroupQueries {

  override def upsert(entity: GroupEntity): D[GroupEntity] =
    D.lift(upsertQuery(entity).run).as(GroupEntity)

  override def findById(groupId: GroupId): D[Option[GroupEntity]] =
    D.lift(findByIdQuery(groupId).option)

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

  def incrementDeviceCountQuery(groupId: GroupId, updatedAt: UpdatedAt): doobie.Update0 =
    fr"""UPDATE groups SET device_count = device_count + 1, updated_at = $updatedAt WHERE id = $groupId""".update
}
