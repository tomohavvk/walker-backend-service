package org.tomohavvk.walker.streams

import cats.effect.kernel.Clock
import cats.effect.kernel.Sync
import cats.effect.kernel.Temporal
import cats.implicits.catsSyntaxFlatMapOps
import cats.implicits.toFunctorOps
import io.odin.Logger
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

class DeviceLocationEventStream[F[_]: Temporal: Clock, B[_]](
  locationService: LocationService[F],
  eventConsumer:   EventConsumer[F, Key, Event],
  logger:          Logger[F]) {
  import eventConsumer._

  val stream: F[Unit] =
    consumer.subscribeTo(topic) >>
      consumer.stream
        .evalMap { committable =>
          committable.record.value.event match {
            case event: DeviceLocationEvent =>
              //  logger.info(event.locations.toString()) >>
              locationService
                .upsertBatch(event.deviceId, makeEntities(event))
                .as(committable.offset)
          }
        }
        .through(commitBatchWithin(200, 1.seconds))
        .debug()
        .compile
        .drain

  private def makeEntities(event: DeviceLocationEvent): List[DeviceLocationEntity] =
    event.locations
      .map { location =>
        location
          .into[DeviceLocationEntity]
          .withFieldConst(_.deviceId, event.deviceId)
          .withFieldComputed(_.bearing, _.bearing.getOrElse(Bearing(0)))
          .withFieldComputed(_.altitudeAccuracy, _.altitudeAccuracy.getOrElse(AltitudeAccuracy(0)))
          .withFieldComputed(_.time, l => LocalDateTime.ofInstant(Instant.ofEpochMilli(l.time.value), ZoneOffset.UTC))
          .transform
      }
}
