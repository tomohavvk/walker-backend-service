package org.tomohavvk.walker.protocol.views

import org.tomohavvk.walker.protocol.Types._

case class GroupView(
  id:            GroupId,
  ownerDeviceId: DeviceId,
  name:          GroupName,
  deviceCount:   DeviceCount,
  isPrivate:     IsPrivate,
  createdAt:     CreatedAt,
  updatedAt:     UpdatedAt)
