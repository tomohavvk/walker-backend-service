package org.tomohavvk.walker.serialization.avro

import org.scalacheck.Arbitrary
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

trait ArbitrarySpec {
  implicit val arbEventId: Arbitrary[EventId]                   = EventId.deriving
  implicit val arbDeviceId: Arbitrary[DeviceId]                 = DeviceId.deriving
  implicit val arbLatitude: Arbitrary[Latitude]                 = Latitude.deriving
  implicit val arbLongitude: Arbitrary[Longitude]               = Longitude.deriving
  implicit val arbUnixTime: Arbitrary[UnixTime]                 = UnixTime.deriving
  implicit val arbAccuracy: Arbitrary[Accuracy]                 = Accuracy.deriving
  implicit val arbAltitudeTime: Arbitrary[Altitude]             = Altitude.deriving
  implicit val arbSpeed: Arbitrary[Speed]                       = Speed.deriving
  implicit val arbBearing: Arbitrary[Bearing]                   = Bearing.deriving
  implicit val arbAltitudeAccuracy: Arbitrary[AltitudeAccuracy] = AltitudeAccuracy.deriving

}
