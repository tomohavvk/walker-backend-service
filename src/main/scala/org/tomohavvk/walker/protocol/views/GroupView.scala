package org.tomohavvk.walker.protocol.views

import org.tomohavvk.walker.protocol.Types.DeviceCount
import org.tomohavvk.walker.protocol.Types.DeviceId
import org.tomohavvk.walker.protocol.Types.GroupId
import org.tomohavvk.walker.protocol.Types.GroupName

import java.time.LocalDateTime

case class GroupView(
  id:            GroupId,
  name:          GroupName,
  deviceCount:   DeviceCount,
  ownerDeviceId: DeviceId,
  createdAt:     LocalDateTime)
