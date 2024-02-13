package org.tomohavvk.walker.http.endpoints.codecs

import org.tomohavvk.walker.http.endpoints.schemas.EndpointSchemas
import org.tomohavvk.walker.protocol.views.DeviceLocationView
import sttp.tapir.Codec.JsonCodec

case class LocationCodecs(
)(implicit val codecDeviceLocationView: JsonCodec[DeviceLocationView])
    extends EndpointSchemas
