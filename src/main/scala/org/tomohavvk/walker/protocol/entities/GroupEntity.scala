package org.tomohavvk.walker.protocol.entities

import io.scalaland.chimney.dsl.TransformerOps
import org.tomohavvk.walker.protocol.Types._
import org.tomohavvk.walker.protocol.views.GroupView

case class GroupEntity(
  id:            GroupId,
  publicId:      GroupPublicId,
  ownerDeviceId: DeviceId,
  name:          GroupName,
  description:   Description,
  deviceCount:   DeviceCount,
  isPublic:      IsPublic,
  createdAt:     CreatedAt,
  updatedAt:     UpdatedAt,
  isJoined:      Option[IsJoined] = None)

object GroupEntity {

  implicit class GroupEntityExt(val entity: GroupEntity) {
    def asView: GroupView = entity.transformInto[GroupView]
  }
}
