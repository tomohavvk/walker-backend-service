package org.tomohavvk.walker.http.endpoints.bodies

import com.aventrix.jnanoid.jnanoid.NanoIdUtils
import org.tomohavvk.walker.protocol.Types._
import org.tomohavvk.walker.protocol.views.DeviceLocationView
import sttp.tapir.Codec.JsonCodec
import sttp.tapir.EndpointIO.Body
import sttp.tapir.customCodecJsonBody

import java.time.LocalDateTime
import java.time.ZoneOffset

trait LocationBodies extends LocationExamples {

  protected def bodyForDeviceLocationView(
    implicit codec: JsonCodec[DeviceLocationView]
  ): Body[String, DeviceLocationView] =
    customCodecJsonBody[DeviceLocationView].example(exampleDeviceLocationView)
}

trait LocationExamples {

  protected val exampleDeviceLocationView: DeviceLocationView =
    DeviceLocationView(
      deviceId = DeviceId(NanoIdUtils.randomNanoId()),
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
