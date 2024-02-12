package org.tomohavvk.walker.http.endpoints.metas

import org.tomohavvk.walker.protocol.Types.DeviceId
import org.tomohavvk.walker.protocol.Types.TraceId
import org.tomohavvk.walker.protocol.commands.CreateDeviceCommand

case class CreateDeviceCommandMeta(deviceId: DeviceId, traceId: TraceId, command: CreateDeviceCommand)
