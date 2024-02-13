package org.tomohavvk.walker.http.endpoints.codecs

import org.tomohavvk.walker.http.endpoints.schemas.EndpointSchemas
import org.tomohavvk.walker.protocol.Types.DeviceId
import org.tomohavvk.walker.protocol.Types.GroupId
import org.tomohavvk.walker.protocol.Types.TraceId
import org.tomohavvk.walker.protocol.Types.XAuthDeviceId
import sttp.tapir.Codec.PlainCodec

case class CommonCodecs() extends EndpointSchemas {
  implicit val codecTraceId: PlainCodec[TraceId]             = TraceId.deriving
  implicit val codecDeviceId: PlainCodec[DeviceId]           = DeviceId.deriving
  implicit val codecGroupId: PlainCodec[GroupId]             = GroupId.deriving
  implicit val codecXAuthDeviceId: PlainCodec[XAuthDeviceId] = XAuthDeviceId.deriving
}
