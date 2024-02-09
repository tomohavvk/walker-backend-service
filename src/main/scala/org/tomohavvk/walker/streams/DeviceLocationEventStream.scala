package org.tomohavvk.walker.streams

import cats.effect.kernel.Clock
import cats.effect.kernel.Sync
import cats.implicits.catsSyntaxFlatMapOps
import cats.implicits.toFunctorOps
import io.odin.Logger
import org.tomohavvk.walker.EventConsumer
import org.tomohavvk.walker.protocol.Types.Key
import org.tomohavvk.walker.protocol.events.DeviceLocationEvent
import org.tomohavvk.walker.protocol.events.Event

class DeviceLocationEventStream[F[_]: Sync: Clock](eventConsumer: EventConsumer[F, Key, Event], logger: Logger[F]) {
  import eventConsumer._

  val stream: F[Unit] =
    consumer.subscribeTo(topic) >>
      consumer.stream
        .evalMap { committable =>
          committable.record.value.event match {
            case event: DeviceLocationEvent => logger.info(event.toString).as(committable)
          }
        }
        .evalMap(_.offset.commit)
        .compile
        .drain
}
