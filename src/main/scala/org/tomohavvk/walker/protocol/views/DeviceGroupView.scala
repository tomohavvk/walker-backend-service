package org.tomohavvk.walker.protocol.views

import org.tomohavvk.walker.protocol.Types._

case class DeviceGroupView(
  deviceId:  DeviceId,
  groupId:   GroupId,
  createdAt: CreatedAt)
