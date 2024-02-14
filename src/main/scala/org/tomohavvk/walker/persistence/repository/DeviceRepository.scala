package org.tomohavvk.walker.persistence.repository

import cats.Monad
import cats.implicits.toFunctorOps
import org.tomohavvk.walker.protocol.Types.DeviceId
import org.tomohavvk.walker.protocol.entities.DeviceEntity
import doobie.implicits._
import org.tomohavvk.walker.persistence._
import org.tomohavvk.walker.protocol.errors.AppError
import org.tomohavvk.walker.utils.LiftConnectionIO

trait DeviceRepository[D[_]] {
  def upsert(entity:     DeviceEntity): D[DeviceEntity]
  def findById(deviceId: DeviceId): D[Option[DeviceEntity]]
}

class DoobieDeviceRepository[D[_]: Monad](implicit D: LiftConnectionIO[D, AppError])
    extends DeviceRepository[D]
    with DeviceQueries {

  override def upsert(entity: DeviceEntity): D[DeviceEntity] =
    D.lift(upsertQuery(entity).run).as(entity)

  override def findById(deviceId: DeviceId): D[Option[DeviceEntity]] =
    D.lift(findByIdQuery(deviceId).option)
}

trait DeviceQueries extends DoobieMeta {

  def upsertQuery(entity: DeviceEntity): doobie.Update0 = {
    import entity._
    fr"""INSERT INTO devices (id, name, created_at) VALUES ($id, $name, $createdAt)""".update
  }

  def findByIdQuery(deviceId: DeviceId): doobie.Query0[DeviceEntity] =
    fr"""SELECT id, name, created_at FROM devices WHERE id = $deviceId""".query[DeviceEntity]
}
