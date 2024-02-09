package org.tomohavvk.walker.protocol.events

import org.tomohavvk.walker.protocol.DeviceLocation
import org.tomohavvk.walker.protocol.Types.Accuracy
import org.tomohavvk.walker.protocol.Types.Altitude
import org.tomohavvk.walker.protocol.Types.AltitudeAccuracy
import org.tomohavvk.walker.protocol.Types.Bearing
import org.tomohavvk.walker.protocol.Types.DeviceId
import org.tomohavvk.walker.protocol.Types.EventId
import org.tomohavvk.walker.protocol.Types.Latitude
import org.tomohavvk.walker.protocol.Types.Longitude
import org.tomohavvk.walker.protocol.Types.Speed
import org.tomohavvk.walker.protocol.Types.UnixTime

sealed trait AppEvent

case class Metadata(id: EventId, producedAt: UnixTime)

case class DeviceLocationEvent(
  deviceId:  DeviceId,
  locations: List[DeviceLocation])
    extends AppEvent

case class Event(event: AppEvent, meta: Metadata)
