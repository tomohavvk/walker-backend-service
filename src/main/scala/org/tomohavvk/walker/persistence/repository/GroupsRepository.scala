package org.tomohavvk.walker.persistence.repository

import org.tomohavvk.walker.protocol.Types.GroupId
import org.tomohavvk.walker.protocol.entities.GroupEntity
import doobie.implicits._
import org.tomohavvk.walker.persistence._
import org.tomohavvk.walker.protocol.errors.AppError
import org.tomohavvk.walker.utils.LiftConnectionIO

trait GroupRepository[D[_]] {
  def upsert(entity:    GroupEntity): D[Int]
  def findById(groupId: GroupId): D[Option[GroupEntity]]
}

class DoobieGroupRepository[D[_]](implicit D: LiftConnectionIO[D, AppError])
    extends GroupRepository[D]
    with GroupQueries {

  override def upsert(entity: GroupEntity): D[Int] =
    D.lift(upsertQuery(entity).run)

  override def findById(groupId: GroupId): D[Option[GroupEntity]] =
    D.lift(findByIdQuery(groupId).option)
}

trait GroupQueries extends DoobieMeta {

  def upsertQuery(entity: GroupEntity): doobie.Update0 = {
    import entity._
    fr"""INSERT INTO groups (id, owner_device_id, name, device_count, is_private, created_at, updated_at) VALUES ($id, $ownerDeviceId, $name, $deviceCount, $isPrivate, $createdAt, $updatedAt)""".update
  }

  def findByIdQuery(groupId: GroupId): doobie.Query0[GroupEntity] =
    fr"""SELECT id, owner_device_id, name, device_count, is_private, created_at, updated_at FROM groups WHERE id = $groupId"""
      .query[GroupEntity]
}
