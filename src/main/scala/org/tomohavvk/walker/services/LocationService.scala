package org.tomohavvk.walker.services

import org.tomohavvk.walker.protocol.Types.DeviceId
import org.tomohavvk.walker.protocol.entities.DeviceLocationEntity
import org.tomohavvk.walker.protocol.views.DeviceLocationView
import org.tomohavvk.walker.utils.ContextFlow

trait LocationService[F[_]] {
  def lastLocation(deviceId: DeviceId): ContextFlow[F, DeviceLocationView]
  def upsertBatch(deviceId:  DeviceId, locations: List[DeviceLocationEntity]): F[Int]
}
