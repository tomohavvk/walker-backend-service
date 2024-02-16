package org.tomohavvk.walker.protocol.entities

import io.scalaland.chimney.dsl.TransformerOps
import org.tomohavvk.walker.protocol.Types._
import org.tomohavvk.walker.protocol.views.DeviceGroupView

case class DeviceGroupEntity(
  deviceId:  DeviceId,
  groupId:   GroupId,
  createdAt: CreatedAt)

object DeviceGroupEntity {

  implicit class DeviceGroupEntityExt(val entity: DeviceGroupEntity) {
    def asView: DeviceGroupView = entity.transformInto[DeviceGroupView]
  }
}
