package org.tomohavvk.walker.persistence.repository

import cats.Monad
import cats.implicits.toFunctorOps
import doobie.implicits._
import org.tomohavvk.walker.persistence._
import org.tomohavvk.walker.protocol.Types.DeviceId
import org.tomohavvk.walker.protocol.Types.GroupId
import org.tomohavvk.walker.protocol.entities.DeviceGroupEntity
import org.tomohavvk.walker.protocol.errors.AppError
import org.tomohavvk.walker.utils.LiftConnectionIO

trait DeviceGroupRepository[D[_]] {
  def upsert(entity:     DeviceGroupEntity): D[DeviceGroupEntity]
  def findById(deviceId: DeviceId, groupId: GroupId): D[Option[DeviceGroupEntity]]
}

class DoobieDeviceGroupRepository[D[_]: Monad](implicit D: LiftConnectionIO[D, AppError])
    extends DeviceGroupRepository[D]
    with DeviceGroupQueries {

  override def upsert(entity: DeviceGroupEntity): D[DeviceGroupEntity] =
    D.lift(upsertQuery(entity).run).as(entity)

  override def findById(deviceId: DeviceId, groupId: GroupId): D[Option[DeviceGroupEntity]] =
    D.lift(findByIdQuery(deviceId, groupId).option)
}

trait DeviceGroupQueries extends DoobieMeta {

  def upsertQuery(entity: DeviceGroupEntity): doobie.Update0 = {
    import entity._
    fr"""INSERT INTO devices_groups (device_id, group_id, created_at) VALUES ($deviceId, $groupId, $createdAt)""".update
  }

  def findByIdQuery(deviceId: DeviceId, groupId: GroupId): doobie.Query0[DeviceGroupEntity] =
    fr"""SELECT device_id, group_id, created_at FROM devices_groups WHERE device_id = $deviceId AND group_id = $groupId"""
      .query[DeviceGroupEntity]
}
