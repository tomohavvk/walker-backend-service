package org.tomohavvk.walker.module

import org.tomohavvk.walker.http.endpoints.LocationEndpoints
import org.tomohavvk.walker.http.endpoints.ProbeEndpoints

case class Endpoints(probe: ProbeEndpoints, marketFeed: LocationEndpoints)

object EndpointModule {

  def make[F[_]](implicit environment: Environment[F]): Endpoints = {
    import environment.codecs._
    Endpoints(
      new ProbeEndpoints()(probe, errorCodecs),
      new LocationEndpoints()(locationCodecs, errorCodecs)
    )
  }
}
