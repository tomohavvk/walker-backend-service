package org.tomohavvk.walker.protocol.ws

import enumeratum.values.StringEnum
import enumeratum.values.StringEnumEntry

import org.tomohavvk.walker.protocol.views.DeviceGroupView
import org.tomohavvk.walker.protocol.views.GroupView

import scala.collection.immutable.IndexedSeq

sealed trait WSMessageOut {
  val `type`: MessageOutType
}

sealed abstract class MessageOutType(val value: String) extends StringEnumEntry

object MessageOutType extends StringEnum[MessageOutType] {
  case object Error extends MessageOutType("error")

  case object LocationPersisted extends MessageOutType("location_persisted")

  case object GroupCreated extends MessageOutType("group_created")
  case object GroupJoined  extends MessageOutType("group_joined")

  case object GroupsGot      extends MessageOutType("groups_got")
  case object GroupsSearched extends MessageOutType("groups_searched")

  override def values: IndexedSeq[MessageOutType] = findValues
}

// TODO make error more informative
case class WSError(message: String) extends WSMessageOut {
  override val `type`: MessageOutType = MessageOutType.Error
}

case class LocationPersisted() extends WSMessageOut {
  override val `type`: MessageOutType = MessageOutType.LocationPersisted
}

case class GroupCreated(group: GroupView) extends WSMessageOut {
  override val `type`: MessageOutType = MessageOutType.GroupCreated
}

case class GroupJoined(deviceGroup: DeviceGroupView) extends WSMessageOut {
  override val `type`: MessageOutType = MessageOutType.GroupJoined
}

case class GroupsGot(groups: List[GroupView]) extends WSMessageOut {
  override val `type`: MessageOutType = MessageOutType.GroupsGot
}

case class GroupsSearched(groups: List[GroupView]) extends WSMessageOut {
  override val `type`: MessageOutType = MessageOutType.GroupsSearched
}
