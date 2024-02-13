package org.tomohavvk.walker.module

import org.tomohavvk.walker.http.endpoints.DeviceEndpoints
import org.tomohavvk.walker.http.endpoints.LocationEndpoints
import org.tomohavvk.walker.http.endpoints.ProbeEndpoints

case class Endpoints(probe: ProbeEndpoints, location: LocationEndpoints, device: DeviceEndpoints)

object EndpointModule {

  def make(codecs: Codecs): Endpoints =
    Endpoints(
      new ProbeEndpoints()(codecs.probe, codecs.errorCodecs),
      new LocationEndpoints()(codecs.locationCodecs, codecs.errorCodecs),
      new DeviceEndpoints()(codecs.deviceCodecs, codecs.errorCodecs)
    )
}
