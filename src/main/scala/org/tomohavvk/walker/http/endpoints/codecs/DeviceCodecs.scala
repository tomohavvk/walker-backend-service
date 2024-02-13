package org.tomohavvk.walker.http.endpoints.codecs

import org.tomohavvk.walker.http.endpoints.schemas.EndpointSchemas
import org.tomohavvk.walker.protocol.commands.CreateDeviceCommand
import org.tomohavvk.walker.protocol.views.DeviceView
import sttp.tapir.Codec.JsonCodec

case class DeviceCodecs(
)(implicit val codecDeviceView: JsonCodec[DeviceView],
  val codecCreateDeviceCommand: JsonCodec[CreateDeviceCommand])
    extends EndpointSchemas
