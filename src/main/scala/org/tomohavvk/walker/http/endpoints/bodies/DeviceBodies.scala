package org.tomohavvk.walker.http.endpoints.bodies

import org.tomohavvk.walker.protocol.Types.DeviceId
import org.tomohavvk.walker.protocol.Types.DeviceName
import org.tomohavvk.walker.protocol.commands.CreateDeviceCommand
import org.tomohavvk.walker.protocol.views.DeviceView
import sttp.tapir.Codec.JsonCodec
import sttp.tapir.EndpointIO.Body
import sttp.tapir.customCodecJsonBody

import java.time.LocalDateTime

trait DeviceBodies extends DeviceExamples {

  protected def bodyForDeviceView(
    implicit codec: JsonCodec[DeviceView]
  ): Body[String, DeviceView] =
    customCodecJsonBody[DeviceView].example(exampleDeviceView)

  protected def bodyForCreateDeviceCommand(
    implicit codec: JsonCodec[CreateDeviceCommand]
  ): Body[String, CreateDeviceCommand] =
    customCodecJsonBody[CreateDeviceCommand].example(exampleCreateDeviceCommand)
}

trait DeviceExamples {

  protected val exampleDeviceView: DeviceView =
    DeviceView(
      id = DeviceId("C471D192-6B42-47C6-89EF-2BCD49DB603D"),
      name = DeviceName("Walker Device"),
      createdAt = LocalDateTime.now()
    )

  protected val exampleCreateDeviceCommand: CreateDeviceCommand =
    CreateDeviceCommand(
      name = DeviceName("Walker Device")
    )
}
