package org.tomohavvk.walker.persistence.repository

import doobie.implicits.toSqlInterpolator
import doobie.util.update.Update
import org.tomohavvk.walker.persistence.DoobieMeta
import org.tomohavvk.walker.protocol.Types.DeviceId
import org.tomohavvk.walker.protocol.entities.DeviceLocationEntity

trait DeviceLocationStatements extends DoobieMeta {

  def upsertQuery(entities: List[DeviceLocationEntity]): doobie.ConnectionIO[Int] = {
    val sqlStatement =
      """INSERT INTO devices_locations (deviceId, latitude, longitude, accuracy, altitude, speed, bearing, altitude_accuracy, time)
      VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
      ON CONFLICT(device_id,time) DO NOTHING""".stripMargin

    Update[DeviceLocationEntity](sqlStatement).updateMany(entities)
  }

  def findLastByIdSQuery(deviceId: DeviceId): doobie.Query0[DeviceLocationEntity] =
    fr"""SELECT deviceId, latitude, longitude, accuracy, altitude, speed, bearing, altitude_accuracy, time
         FROM devices_locations WHERE device_id = $deviceId ORDER BY time DESC LIMIT 1"""
      .query[DeviceLocationEntity]
}
