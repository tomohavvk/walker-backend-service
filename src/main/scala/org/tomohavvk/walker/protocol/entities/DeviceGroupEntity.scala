package org.tomohavvk.walker.protocol.entities

import org.tomohavvk.walker.protocol.Types._

case class DeviceGroupEntity(
  deviceId:  DeviceId,
  groupId:   GroupId,
  createdAt: CreatedAt)
