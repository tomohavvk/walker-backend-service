package org.tomohavvk.walker.http.endpoints.bodies

import org.tomohavvk.walker.protocol.Types.CreatedAt
import org.tomohavvk.walker.protocol.Types.DeviceCount
import org.tomohavvk.walker.protocol.Types.DeviceId
import org.tomohavvk.walker.protocol.Types.GroupId
import org.tomohavvk.walker.protocol.Types.GroupName
import org.tomohavvk.walker.protocol.Types.IsPrivate
import org.tomohavvk.walker.protocol.Types.UpdatedAt
import org.tomohavvk.walker.protocol.commands.CreateGroupCommand
import org.tomohavvk.walker.protocol.views.DeviceGroupView
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

  protected def bodyForListOfGroupView(
    implicit codec: JsonCodec[List[GroupView]]
  ): Body[String, List[GroupView]] =
    customCodecJsonBody[List[GroupView]].example(List(exampleGroupView))

  protected def bodyForDeviceGroupView(
    implicit codec: JsonCodec[DeviceGroupView]
  ): Body[String, DeviceGroupView] =
    customCodecJsonBody[DeviceGroupView].example(exampleDeviceGroupView)

  protected def bodyForCreateGroupCommand(
    implicit codec: JsonCodec[CreateGroupCommand]
  ): Body[String, CreateGroupCommand] =
    customCodecJsonBody[CreateGroupCommand].example(exampleCreateGroupCommand)
}

trait GroupExamples {

  protected val exampleGroupView: GroupView =
    GroupView(
      id = GroupId("729d378c-1a64-4245-9569-2d1109dc9bdc"),
      name = GroupName("Walker Group"),
      deviceCount = DeviceCount(200),
      ownerDeviceId = DeviceId("C471D192-6B42-47C6-89EF-2BCD49DB603D"),
      isPrivate = IsPrivate(true),
      createdAt = CreatedAt(LocalDateTime.now()),
      updatedAt = UpdatedAt(LocalDateTime.now())
    )

  protected val exampleDeviceGroupView: DeviceGroupView =
    DeviceGroupView(
      deviceId = DeviceId("C471D192-6B42-47C6-89EF-2BCD49DB603D"),
      groupId = GroupId("729d378c-1a64-4245-9569-2d1109dc9bdc"),
      createdAt = CreatedAt(LocalDateTime.now())
    )

  protected val exampleCreateGroupCommand: CreateGroupCommand =
    CreateGroupCommand(
      name = GroupName("Walker Group"),
      isPrivate = IsPrivate(true)
    )
}
