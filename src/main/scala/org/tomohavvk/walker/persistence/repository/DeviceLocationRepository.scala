package org.tomohavvk.walker.persistence.repository

import org.tomohavvk.walker.protocol.Types.DeviceId
import org.tomohavvk.walker.protocol.entities.DeviceLocationEntity

import doobie.util.update.Update
import doobie.implicits._
import org.tomohavvk.walker.persistence._

import org.tomohavvk.walker.protocol.errors.AppError
import org.tomohavvk.walker.utils.LiftConnectionIO

trait DeviceLocationRepository[F[_]] {
  def findLastById(deviceId: DeviceId): F[Option[DeviceLocationEntity]]
  def upsertBatch(entities:  List[DeviceLocationEntity]): F[Int]
}

class DoobieDeviceLocationRepository[F[_]](implicit F: LiftConnectionIO[F, AppError])
    extends DeviceLocationRepository[F]
    with DeviceLocationQueries {

  override def upsertBatch(entities: List[DeviceLocationEntity]): F[Int] =
    F.lift(upsertQuery(entities))

  override def findLastById(deviceId: DeviceId): F[Option[DeviceLocationEntity]] =
    F.lift(findLastByIdSQuery(deviceId).option)
}

trait DeviceLocationQueries extends DoobieMeta {

  def upsertQuery(entities: List[DeviceLocationEntity]): doobie.ConnectionIO[Int] = {
    val sqlStatement =
      """INSERT INTO devices_locations (device_id, latitude, longitude, accuracy, altitude, speed, bearing, altitude_accuracy, time)
      VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
      ON CONFLICT(device_id,time) DO NOTHING""".stripMargin

    Update[DeviceLocationEntity](sqlStatement).updateMany(entities)
  }

  def findLastByIdSQuery(deviceId: DeviceId): doobie.Query0[DeviceLocationEntity] =
    fr"""SELECT device_id, latitude, longitude, accuracy, altitude, speed, bearing, altitude_accuracy, time
         FROM devices_locations WHERE device_id = $deviceId ORDER BY time DESC LIMIT 1"""
      .query[DeviceLocationEntity]
}
