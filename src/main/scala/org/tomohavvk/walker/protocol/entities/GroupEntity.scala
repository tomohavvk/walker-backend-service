package org.tomohavvk.walker.protocol.entities

import org.tomohavvk.walker.protocol.Types._

import java.time.LocalDateTime

case class GroupEntity(
  id:            GroupId,
  name:          GroupName,
  deviceCount:   DeviceCount,
  ownerDeviceId: DeviceId,
  createdAt:     LocalDateTime)
