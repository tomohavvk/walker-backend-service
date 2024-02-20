package org.tomohavvk.walker.protocol.views

import org.tomohavvk.walker.protocol.Types._

case class GroupView(
  id:            GroupId,
  publicId:      GroupPublicId,
  ownerDeviceId: DeviceId,
  name:          GroupName,
  description:   Description,
  deviceCount:   DeviceCount,
  isPublic:      IsPublic,
  createdAt:     CreatedAt,
  updatedAt:     UpdatedAt)
