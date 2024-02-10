package org.tomohavvk.walker.protocol.views

import org.tomohavvk.walker.protocol.Types._

case class DeviceLocationView(
  deviceId:         DeviceId,
  latitude:         Latitude,
  longitude:        Longitude,
  accuracy:         Accuracy,
  altitude:         Altitude,
  speed:            Speed,
  time:             UnixTime,
  bearing:          Option[Bearing] = None,
  altitudeAccuracy: Option[AltitudeAccuracy] = None)
