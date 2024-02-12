package org.tomohavvk.walker.protocol.views

import org.tomohavvk.walker.protocol.Types.DeviceId
import org.tomohavvk.walker.protocol.Types.DeviceName

import java.time.LocalDateTime

case class DeviceView(id: DeviceId, name: DeviceName, createdAt: LocalDateTime)
