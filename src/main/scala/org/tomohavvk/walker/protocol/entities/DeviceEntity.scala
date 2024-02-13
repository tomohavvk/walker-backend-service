package org.tomohavvk.walker.protocol.entities

import org.tomohavvk.walker.protocol.Types._

case class DeviceEntity(
  id:        DeviceId,
  name:      DeviceName,
  createdAt: CreatedAt)
