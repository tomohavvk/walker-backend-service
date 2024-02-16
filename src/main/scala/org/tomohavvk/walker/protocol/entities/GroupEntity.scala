package org.tomohavvk.walker.protocol.entities

import io.scalaland.chimney.dsl.TransformerOps
import org.tomohavvk.walker.protocol.Types._
import org.tomohavvk.walker.protocol.views.GroupView

case class GroupEntity(
  id:            GroupId,
  ownerDeviceId: DeviceId,
  name:          GroupName,
  deviceCount:   DeviceCount,
  isPrivate:     IsPrivate,
  createdAt:     CreatedAt,
  updatedAt:     UpdatedAt)

object GroupEntity {

  implicit class GroupEntityExt(val entity: GroupEntity) {
    def asView: GroupView = entity.transformInto[GroupView]
  }
}
