package org.tomohavvk.walker.protocol.events

import org.tomohavvk.walker.protocol.DeviceLocation
import org.tomohavvk.walker.protocol.Types.DeviceId
import org.tomohavvk.walker.protocol.Types.EventId
import org.tomohavvk.walker.protocol.Types.UnixTime

sealed trait AppEvent

case class Metadata(id: EventId, producedAt: UnixTime)

case class DeviceLocationEvent(
  deviceId:  DeviceId,
  locations: List[DeviceLocation])
    extends AppEvent

case class Event(event: AppEvent, meta: Metadata)
