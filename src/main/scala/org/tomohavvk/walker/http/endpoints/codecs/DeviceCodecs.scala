package org.tomohavvk.walker.http.endpoints.codecs

import org.tomohavvk.walker.http.endpoints.schemas.EndpointSchemas
import org.tomohavvk.walker.protocol.Types.DeviceId
import org.tomohavvk.walker.protocol.Types.TraceId
import org.tomohavvk.walker.protocol.commands.CreateDeviceCommand
import org.tomohavvk.walker.protocol.views.DeviceView
import sttp.tapir.Codec.JsonCodec
import sttp.tapir.Codec.PlainCodec

case class DeviceCodecs(
)(implicit val codecDeviceView: JsonCodec[DeviceView],
  val codecCreateDeviceCommand: JsonCodec[CreateDeviceCommand])
    extends EndpointSchemas {
  implicit val codecTraceId: PlainCodec[TraceId]   = TraceId.deriving
  implicit val codecDeviceId: PlainCodec[DeviceId] = DeviceId.deriving
}
