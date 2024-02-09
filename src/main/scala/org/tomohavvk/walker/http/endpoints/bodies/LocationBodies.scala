package org.tomohavvk.walker.http.endpoints.bodies

import org.tomohavvk.walker.http.endpoints.bodies.examples.LocationExamples
import org.tomohavvk.walker.protocol.error.views.AcknowledgeView
import org.tomohavvk.walker.protocol.error.views.DeviceLocationView
import sttp.tapir.Codec.JsonCodec
import sttp.tapir.EndpointIO.Body
import sttp.tapir.customCodecJsonBody

trait LocationBodies extends LocationExamples {

  protected def bodyForDeviceLocationView(
    implicit codec: JsonCodec[DeviceLocationView]
  ): Body[String, DeviceLocationView] =
    customCodecJsonBody[DeviceLocationView].example(exampleDeviceLocationView)
}
