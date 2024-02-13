package org.tomohavvk.walker.http.endpoints.bodies

import com.aventrix.jnanoid.jnanoid.NanoIdUtils
import org.tomohavvk.walker.protocol.Types.DeviceId
import org.tomohavvk.walker.protocol.Types.GroupId
import org.tomohavvk.walker.protocol.Types.GroupName
import org.tomohavvk.walker.protocol.views.GroupView
import sttp.tapir.Codec.JsonCodec
import sttp.tapir.EndpointIO.Body
import sttp.tapir.customCodecJsonBody

import java.time.LocalDateTime

trait GroupBodies extends GroupExamples {

  protected def bodyForGroupView(
    implicit codec: JsonCodec[GroupView]
  ): Body[String, GroupView] =
    customCodecJsonBody[GroupView].example(exampleGroupView)
}

trait GroupExamples {

  protected val exampleGroupView: GroupView =
    GroupView(
      id = GroupId(NanoIdUtils.randomNanoId()),
      name = GroupName("Walker"),
      ownerDeviceId = DeviceId(NanoIdUtils.randomNanoId()),
      createdAt = LocalDateTime.now()
    )
}
