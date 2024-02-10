package org.tomohavvk.walker.protocol.entities

import org.tomohavvk.walker.protocol.Types.Accuracy
import org.tomohavvk.walker.protocol.Types.Altitude
import org.tomohavvk.walker.protocol.Types.AltitudeAccuracy
import org.tomohavvk.walker.protocol.Types.Bearing
import org.tomohavvk.walker.protocol.Types.DeviceId
import org.tomohavvk.walker.protocol.Types.Latitude
import org.tomohavvk.walker.protocol.Types.Longitude
import org.tomohavvk.walker.protocol.Types.Speed
import org.tomohavvk.walker.protocol.Types.UnixTime

case class DeviceLocationEntity(
  deviceId:         DeviceId,
  latitude:         Latitude,
  longitude:        Longitude,
  accuracy:         Accuracy,
  altitude:         Altitude,
  speed:            Speed,
  bearing:          Bearing,
  altitudeAccuracy: AltitudeAccuracy,
  time:             UnixTime)
