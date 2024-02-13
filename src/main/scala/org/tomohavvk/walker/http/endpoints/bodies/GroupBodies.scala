package org.tomohavvk.walker.http.endpoints.bodies

import com.aventrix.jnanoid.jnanoid.NanoIdUtils
import org.tomohavvk.walker.protocol.Types.DeviceCount
import org.tomohavvk.walker.protocol.Types.DeviceId
import org.tomohavvk.walker.protocol.Types.GroupId
import org.tomohavvk.walker.protocol.Types.GroupName
import org.tomohavvk.walker.protocol.commands.CreateGroupCommand
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

  protected def bodyForCreateGroupCommand(
    implicit codec: JsonCodec[CreateGroupCommand]
  ): Body[String, CreateGroupCommand] =
    customCodecJsonBody[CreateGroupCommand].example(exampleCreateGroupCommand)
}

trait GroupExamples {

  protected val exampleGroupView: GroupView =
    GroupView(
      id = GroupId(NanoIdUtils.randomNanoId()),
      name = GroupName("Walker Group"),
      deviceCount = DeviceCount(200),
      ownerDeviceId = DeviceId("C471D192-6B42-47C6-89EF-2BCD49DB603D"),
      createdAt = LocalDateTime.now()
    )

  protected val exampleCreateGroupCommand: CreateGroupCommand =
    CreateGroupCommand(
      name = GroupName("Walker Group"),
      ownerDeviceId = DeviceId("C471D192-6B42-47C6-89EF-2BCD49DB603D")
    )
}
