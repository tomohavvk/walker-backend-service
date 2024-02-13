package org.tomohavvk.walker.protocol.entities

import org.tomohavvk.walker.protocol.Types._

case class GroupEntity(
  id:            GroupId,
  ownerDeviceId: DeviceId,
  name:          GroupName,
  deviceCount:   DeviceCount,
  isPrivate:     IsPrivate,
  createdAt:     CreatedAt,
  updatedAt:     UpdatedAt)
