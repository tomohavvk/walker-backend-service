package org.tomohavvk.walker.persistence.repository

import org.tomohavvk.walker.protocol.Types.DeviceId
import org.tomohavvk.walker.protocol.entities.DeviceLocationEntity

trait DeviceLocationRepository[F[_]] {
  def findLastById(deviceId: DeviceId): F[Option[DeviceLocationEntity]]
  def upsertBatch(entities:  List[DeviceLocationEntity]): F[Int]
}
