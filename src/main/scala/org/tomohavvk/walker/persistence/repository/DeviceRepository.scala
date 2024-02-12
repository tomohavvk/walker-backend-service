package org.tomohavvk.walker.persistence.repository

import org.tomohavvk.walker.protocol.Types.DeviceId
import org.tomohavvk.walker.protocol.entities.DeviceEntity
import doobie.implicits._
import org.tomohavvk.walker.persistence._
import org.tomohavvk.walker.protocol.errors.AppError
import org.tomohavvk.walker.utils.LiftConnectionIO

trait DeviceRepository[F[_]] {
  def upsert(entity:     DeviceEntity): F[Int]
  def findById(deviceId: DeviceId): F[Option[DeviceEntity]]
}

class DoobieDeviceRepository[F[_]](implicit F: LiftConnectionIO[F, AppError])
    extends DeviceRepository[F]
    with DeviceQueries {

  override def upsert(entity: DeviceEntity): F[Int] =
    F.lift(upsertQuery(entity).run)

  override def findById(deviceId: DeviceId): F[Option[DeviceEntity]] =
    F.lift(findByIdQuery(deviceId).option)
}

trait DeviceQueries extends DoobieMeta {

  def upsertQuery(entity: DeviceEntity): doobie.Update0 = {
    import entity._
    fr"""INSERT INTO devices (id, name, created_at) VALUES ($id, $name, $createdAt)""".update
  }

  def findByIdQuery(deviceId: DeviceId): doobie.Query0[DeviceEntity] =
    fr"""SELECT id, name, created_at FROM devices WHERE id = $deviceId""".query[DeviceEntity]
}
