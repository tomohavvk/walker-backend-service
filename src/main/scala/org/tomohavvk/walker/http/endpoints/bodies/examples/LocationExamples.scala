package org.tomohavvk.walker.http.endpoints.bodies.examples

import org.tomohavvk.walker.protocol.Types.Accuracy
import org.tomohavvk.walker.protocol.Types.Altitude
import org.tomohavvk.walker.protocol.Types.AltitudeAccuracy
import org.tomohavvk.walker.protocol.Types.Bearing
import org.tomohavvk.walker.protocol.Types.DeviceId
import org.tomohavvk.walker.protocol.Types.Latitude
import org.tomohavvk.walker.protocol.Types.Longitude
import org.tomohavvk.walker.protocol.Types.Speed
import org.tomohavvk.walker.protocol.Types.UnixTime
import org.tomohavvk.walker.protocol.error.views.AcknowledgeView
import org.tomohavvk.walker.protocol.error.views.DeviceLocationView

import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.UUID

trait LocationExamples {

  protected val exampleDeviceLocationView: DeviceLocationView =
    DeviceLocationView(
      deviceId = DeviceId(UUID.randomUUID().toString),
      latitude = Latitude(48),
      longitude = Longitude(38),
      accuracy = Accuracy(38),
      altitude = Altitude(38),
      speed = Speed(38),
      time = UnixTime(LocalDateTime.now().toEpochSecond(ZoneOffset.UTC)),
      bearing = Some(Bearing(180)),
      altitudeAccuracy = Some(AltitudeAccuracy(180))
    )
}
