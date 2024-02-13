package org.tomohavvk.walker.protocol.entities

import cats.implicits.catsSyntaxOptionId
import io.scalaland.chimney.dsl.TransformerOps
import org.tomohavvk.walker.protocol.Types.Accuracy
import org.tomohavvk.walker.protocol.Types.Altitude
import org.tomohavvk.walker.protocol.Types.AltitudeAccuracy
import org.tomohavvk.walker.protocol.Types.Bearing
import org.tomohavvk.walker.protocol.Types.DeviceId
import org.tomohavvk.walker.protocol.Types.Latitude
import org.tomohavvk.walker.protocol.Types.Longitude
import org.tomohavvk.walker.protocol.Types.Speed
import org.tomohavvk.walker.protocol.Types.UnixTime
import org.tomohavvk.walker.protocol.views.DeviceLocationView

import java.time.LocalDateTime
import java.time.ZoneOffset

case class DeviceLocationEntity(
  deviceId:         DeviceId,
  latitude:         Latitude,
  longitude:        Longitude,
  accuracy:         Accuracy,
  altitude:         Altitude,
  speed:            Speed,
  bearing:          Bearing,
  altitudeAccuracy: AltitudeAccuracy,
  time:             LocalDateTime)

object DeviceLocationEntity {

  implicit class DeviceLocationEntityExt(val entity: DeviceLocationEntity) {

    def asView: DeviceLocationView =
      entity
        .into[DeviceLocationView]
        .withFieldComputed(_.bearing, _.bearing.some)
        .withFieldComputed(_.altitudeAccuracy, _.altitudeAccuracy.some)
        .withFieldComputed(_.time, l => UnixTime(l.time.toInstant(ZoneOffset.UTC).toEpochMilli))
        .transform
  }
}
