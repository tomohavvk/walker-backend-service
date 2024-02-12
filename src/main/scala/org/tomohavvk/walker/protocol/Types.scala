package org.tomohavvk.walker.protocol

import io.estatico.newtype.macros.newtype

import java.util.UUID

object Types {
  type Key = UUID
  @newtype case class EventId(value: Key)
  @newtype case class DeviceId(value: String)
  @newtype case class GroupId(value: String)
  @newtype case class GroupName(value: String)
  @newtype case class DeviceName(value: String)
  @newtype case class Latitude(value: Double)
  @newtype case class Longitude(value: Double)
  @newtype case class Accuracy(value: Double)
  @newtype case class Altitude(value: Double)
  @newtype case class Bearing(value: Double)
  @newtype case class AltitudeAccuracy(value: Double)
  @newtype case class Speed(value: Double)
  @newtype case class UnixTime(value: Long)

  @newtype case class TraceId(value: String)
  @newtype case class ApiErrorMessage(value: String)
  @newtype case class ErrorCode(value: String)
  @newtype case class HttpCode(value: Int)
  @newtype case class LogErrorMessage(value: String)
}
