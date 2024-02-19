package org.tomohavvk.walker.protocol.ws

import enumeratum.values.StringEnum
import enumeratum.values.StringEnumEntry
import org.tomohavvk.walker.protocol.DeviceLocation
import org.tomohavvk.walker.protocol.Types.GroupId
import org.tomohavvk.walker.protocol.Types.Limit
import org.tomohavvk.walker.protocol.Types.Offset
import org.tomohavvk.walker.protocol.Types.Search

import scala.collection.immutable.IndexedSeq

sealed trait WSMessageIn {
  val `type`: MessageInType
}

sealed abstract class MessageInType(val value: String) extends StringEnumEntry

object MessageInType extends StringEnum[MessageInType] {
  case object LocationPersist extends MessageInType("location_persist")

  case object GroupJoin extends MessageInType("group_join")

  case object GroupsGet    extends MessageInType("groups_get")
  case object GroupsSearch extends MessageInType("groups_search")

  override def values: IndexedSeq[MessageInType] = findValues
}

case class LocationPersist(locations: List[DeviceLocation]) extends WSMessageIn {
  override val `type`: MessageInType = MessageInType.LocationPersist
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
