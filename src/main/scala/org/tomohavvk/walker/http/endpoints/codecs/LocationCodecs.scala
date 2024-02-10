package org.tomohavvk.walker.http.endpoints.codecs

import org.tomohavvk.walker.http.endpoints.schemas.EndpointSchemas
import org.tomohavvk.walker.protocol.Types.DeviceId
import org.tomohavvk.walker.protocol.Types.TraceId
import org.tomohavvk.walker.protocol.views.AcknowledgeView
import org.tomohavvk.walker.protocol.views.DeviceLocationView
import sttp.tapir.Codec.JsonCodec
import sttp.tapir.Codec.PlainCodec

case class LocationCodecs(
)(implicit val codecDeviceLocationView: JsonCodec[DeviceLocationView],
  implicit val codecAcknowledgeView:    JsonCodec[AcknowledgeView])
    extends EndpointSchemas {
  implicit val codecTraceId: PlainCodec[TraceId]   = TraceId.deriving
  implicit val codecDeviceId: PlainCodec[DeviceId] = DeviceId.deriving
}
