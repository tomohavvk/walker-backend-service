package org.tomohavvk.walker.serialization.avro

import org.scalacheck.Arbitrary
import org.tomohavvk.walker.protocol.Types.DeviceId
import org.tomohavvk.walker.protocol.Types.EventId
import org.tomohavvk.walker.protocol.Types.Latitude
import org.tomohavvk.walker.protocol.Types.Longitude
import org.tomohavvk.walker.protocol.Types.UnixTime

trait ArbitrarySpec {
  implicit val arbEventId: Arbitrary[EventId]     = EventId.deriving
  implicit val arbDeviceId: Arbitrary[DeviceId]   = DeviceId.deriving
  implicit val arbLatitude: Arbitrary[Latitude]   = Latitude.deriving
  implicit val arbLongitude: Arbitrary[Longitude] = Longitude.deriving
  implicit val arbUnixTime: Arbitrary[UnixTime]   = UnixTime.deriving

}
