package org.tomohavvk.walker.streams

import cats.effect.kernel.Clock
import cats.effect.kernel.Temporal
import cats.implicits.catsSyntaxFlatMapOps
import cats.implicits.toFunctorOps
import io.scalaland.chimney.dsl._
import org.tomohavvk.walker.EventConsumer
import org.tomohavvk.walker.protocol.Types.AltitudeAccuracy
import org.tomohavvk.walker.protocol.Types.Bearing
import org.tomohavvk.walker.protocol.Types.Key
import org.tomohavvk.walker.protocol.entities.DeviceLocationEntity
import org.tomohavvk.walker.protocol.events.DeviceLocationEvent
import org.tomohavvk.walker.protocol.events.Event
import org.tomohavvk.walker.services.LocationService

import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset
import fs2.kafka._

import scala.concurrent.duration.DurationInt

// TODO Remove since unused after websocket implementation
class DeviceLocationEventStream[F[_]: Temporal: Clock, D[_]](
  locationService: LocationService[F],
  eventConsumer:   EventConsumer[F, Key, Event]) {
  import eventConsumer._

  val stream: F[Unit] =
    consumer.subscribeTo(topic) >>
      consumer.stream
        .evalMap { committable =>
          committable.record.value.event match {
            case event: DeviceLocationEvent =>
              locationService
                .upsertBatch(event.deviceId, event.locations)
                .as(committable.offset)
          }
        }
        .through(commitBatchWithin(200, 1.seconds))
        .debug()
        .compile
        .drain
}
