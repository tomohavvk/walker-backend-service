package org.tomohavvk.walker.protocol.commands

import org.tomohavvk.walker.protocol.Types.GroupName
import org.tomohavvk.walker.protocol.Types.DeviceId

case class CreateGroupCommand(name: GroupName, ownerDeviceId: DeviceId)
