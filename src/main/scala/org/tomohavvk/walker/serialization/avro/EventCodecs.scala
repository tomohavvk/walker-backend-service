package org.tomohavvk.walker.serialization.avro

import io.estatico.newtype.Coercible
import org.tomohavvk.walker.protocol.events.AppEvent
import org.tomohavvk.walker.protocol.events.DeviceLocationEvent
import org.tomohavvk.walker.protocol.events.Event
import org.tomohavvk.walker.protocol.events.Metadata
import vulcan.Codec
import cats.syntax.all._
import org.tomohavvk.walker.protocol.DeviceLocation

trait EventCodecs {

  implicit val codecMetadata: Codec[Metadata] = Codec.record(
    name = "Metadata",
    namespace = "org.tomohavvk.walker.protocol.events"
  ) { field =>
    (
      field("id", _.id),
      field("processed_at", _.producedAt)
    ).mapN(Metadata)
  }

  implicit val codecDeviceLocation: Codec[DeviceLocation] = Codec.record(
    name = "DeviceLocation",
    namespace = "org.tomohavvk.walker.protocol.events"
  ) { field =>
    (
      field("latitude", _.latitude),
      field("longitude", _.longitude),
      field("accuracy", _.accuracy),
      field("altitude", _.altitude),
      field("speed", _.speed),
      field("time", _.time),
      field("bearing", _.bearing),
      field("altitudeAccuracy", _.altitudeAccuracy)
    ).mapN(DeviceLocation)
  }

  implicit val codecDeviceLocationEvent: Codec[DeviceLocationEvent] = Codec.record(
    name = "DeviceLocationEvent",
    namespace = "org.tomohavvk.walker.protocol.events"
  ) { field =>
    (
      field("device_id", _.deviceId),
      field("locations", _.locations)
    ).mapN(DeviceLocationEvent)
  }

  implicit val appEventAvroCodec: Codec[AppEvent] = Codec.union(alt => alt[DeviceLocationEvent])

  implicit val eventAvroCodec: Codec[Event] = Codec.record(
    name = "Event",
    namespace = "org.tomohavvk.walker.protocol.events"
  ) { field =>
    (
      field("event", _.event),
      field("meta", _.meta)
    ).mapN(Event)
  }

  implicit def vulcanCoercibleCodec[A, B](
    implicit to: Coercible[A, B],
    from:        Coercible[B, A],
    codec:       Codec[A]
  ): Codec[B] =
    codec.imap(to.apply)(from.apply)
}
