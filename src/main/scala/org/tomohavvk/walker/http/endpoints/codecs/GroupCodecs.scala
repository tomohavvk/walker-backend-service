package org.tomohavvk.walker.http.endpoints.codecs

import org.tomohavvk.walker.http.endpoints.schemas.EndpointSchemas
import org.tomohavvk.walker.protocol.commands.CreateGroupCommand
import org.tomohavvk.walker.protocol.views.DeviceGroupView
import org.tomohavvk.walker.protocol.views.GroupView
import sttp.tapir.Codec.JsonCodec

case class GroupCodecs(
)(implicit val codecGroupView: JsonCodec[GroupView],
  val codecDeviceGroupView:    JsonCodec[DeviceGroupView],
  val codecCreateGroupCommand: JsonCodec[CreateGroupCommand])
    extends EndpointSchemas
