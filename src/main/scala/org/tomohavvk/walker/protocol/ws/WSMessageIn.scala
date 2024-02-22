package org.tomohavvk.walker.protocol.ws

import enumeratum.values.StringEnum
import enumeratum.values.StringEnumEntry
import org.tomohavvk.walker.protocol.DeviceLocation
import org.tomohavvk.walker.protocol.Types.Description
import org.tomohavvk.walker.protocol.Types.GroupId
import org.tomohavvk.walker.protocol.Types.GroupName
import org.tomohavvk.walker.protocol.Types.IsPublic
import org.tomohavvk.walker.protocol.Types.Limit
import org.tomohavvk.walker.protocol.Types.Offset
import org.tomohavvk.walker.protocol.Types.Search
import org.tomohavvk.walker.protocol.Types.GroupPublicId

import scala.collection.immutable.IndexedSeq

sealed trait WSMessageIn {
  val `type`: MessageInType
}

sealed abstract class MessageInType(val value: String) extends StringEnumEntry

object MessageInType extends StringEnum[MessageInType] {
  case object LocationPersist           extends MessageInType("location_persist")
  case object GroupCreate               extends MessageInType("group_create")
  case object GroupJoin                 extends MessageInType("group_join")
  case object GroupsGet                 extends MessageInType("groups_get")
  case object GroupsSearch              extends MessageInType("groups_search")
  case object PublicIdAvailabilityCheck extends MessageInType("public_id_availability_check")

  override def values: IndexedSeq[MessageInType] = findValues
}

case class LocationPersist(locations: List[DeviceLocation]) extends WSMessageIn {
  override val `type`: MessageInType = MessageInType.LocationPersist
}

case class GroupCreate(
  id:          GroupId,
  name:        GroupName,
  isPublic:    IsPublic,
  publicId:    Option[GroupPublicId],
  description: Option[Description])
    extends WSMessageIn {
  override val `type`: MessageInType = MessageInType.GroupCreate
}

case class GroupJoin(groupId: GroupId) extends WSMessageIn {
  override val `type`: MessageInType = MessageInType.GroupJoin
}

case class GroupsGet(limit: Limit, offset: Offset) extends WSMessageIn {
  override val `type`: MessageInType = MessageInType.GroupsGet
}

case class GroupsSearch(search: Search, limit: Limit, offset: Offset) extends WSMessageIn {
  override val `type`: MessageInType = MessageInType.GroupsSearch
}

case class PublicIdAvailabilityCheck(publicId: GroupPublicId) extends WSMessageIn {
  override val `type`: MessageInType = MessageInType.PublicIdAvailabilityCheck
}
