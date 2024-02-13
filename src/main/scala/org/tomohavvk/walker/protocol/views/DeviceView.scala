package org.tomohavvk.walker.protocol.views

import org.tomohavvk.walker.protocol.Types.CreatedAt
import org.tomohavvk.walker.protocol.Types.DeviceId
import org.tomohavvk.walker.protocol.Types.DeviceName

case class DeviceView(id: DeviceId, name: DeviceName, createdAt: CreatedAt)
