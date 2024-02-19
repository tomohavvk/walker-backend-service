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
  case object LocationPersisted extends MessageOutType("location_persisted")

  case object GroupJoined extends MessageOutType("group_joined")

  case object GroupsSearched extends MessageOutType("groups_searched")

  override def values: IndexedSeq[MessageOutType] = findValues
}

case class LocationPersisted() extends WSMessageOut {
  override val `type`: MessageOutType = MessageOutType.LocationPersisted
}

case class GroupJoined(deviceGroup: DeviceGroupView) extends WSMessageOut {
  override val `type`: MessageOutType = MessageOutType.GroupJoined
}

case class GroupsSearched(groups: List[GroupView]) extends WSMessageOut {
  override val `type`: MessageOutType = MessageOutType.GroupsSearched
}
