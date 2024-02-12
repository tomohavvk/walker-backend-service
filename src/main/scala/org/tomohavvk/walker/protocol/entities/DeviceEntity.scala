package org.tomohavvk.walker.protocol.entities

import org.tomohavvk.walker.protocol.Types._

import java.time.LocalDateTime

case class DeviceEntity(
  id:        DeviceId,
  name:      DeviceName,
  createdAt: LocalDateTime)
