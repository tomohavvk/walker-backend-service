package org.tomohavvk.walker.protocol.ws

import enumeratum.values.{StringEnum, StringEnumEntry}
import org.tomohavvk.walker.protocol.DeviceLocation

import scala.collection.immutable.IndexedSeq

sealed trait WSMessageIn {
  val `type`: MessageInType
}

sealed trait WSMessageOut {
  val `type`: MessageOutType
}

sealed abstract class MessageInType(val value: String) extends StringEnumEntry

object MessageInType extends StringEnum[MessageInType] {
  case object PersistDeviceLocation extends MessageInType("persist_device_location")

  override def values: IndexedSeq[MessageInType] = findValues
}

sealed abstract class MessageOutType(val value: String) extends StringEnumEntry

object MessageOutType extends StringEnum[MessageOutType] {
  case object Acknowledge extends MessageOutType("acknowledge")

  override def values: IndexedSeq[MessageOutType] = findValues
}

case class PersistDeviceLocation(locations: List[DeviceLocation]) extends WSMessageIn {
  override val `type`: MessageInType = MessageInType.PersistDeviceLocation
}

case class Acknowledge(acknowledged: Boolean) extends WSMessageOut {
  override val `type`: MessageOutType = MessageOutType.Acknowledge
}
