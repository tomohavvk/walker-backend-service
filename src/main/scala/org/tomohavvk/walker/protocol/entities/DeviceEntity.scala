package org.tomohavvk.walker.protocol.entities

import io.scalaland.chimney.dsl.TransformerOps
import org.tomohavvk.walker.protocol.Types._
import org.tomohavvk.walker.protocol.views.DeviceView

case class DeviceEntity(
  id:        DeviceId,
  name:      DeviceName,
  createdAt: CreatedAt)

object DeviceEntity {

  implicit class DeviceEntityExt(val entity: DeviceEntity) {
    def asView: DeviceView = entity.transformInto[DeviceView]
  }
}
