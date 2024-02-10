package org.tomohavvk.walker.services

import cats.Applicative
import cats.MonadError
import cats.effect.kernel.Clock
import cats.effect.kernel.Sync
import cats.implicits.catsSyntaxFlatMapOps
import cats.implicits.catsSyntaxOptionId
import cats.implicits.toFlatMapOps
import cats.implicits.toFunctorOps
import fs2.kafka.ProducerRecord
import fs2.kafka.ProducerRecords
import io.odin.Logger
import org.tomohavvk.walker.persistence.Transactor
import org.tomohavvk.walker.persistence.repository.DeviceLocationRepository
import org.tomohavvk.walker.protocol.Types.Accuracy
import org.tomohavvk.walker.protocol.Types.Altitude
import org.tomohavvk.walker.protocol.Types.AltitudeAccuracy
import org.tomohavvk.walker.protocol.Types.Bearing
import org.tomohavvk.walker.protocol.Types.DeviceId
import org.tomohavvk.walker.protocol.Types.EventId
import org.tomohavvk.walker.protocol.Types.Key
import org.tomohavvk.walker.protocol.Types.Latitude
import org.tomohavvk.walker.protocol.Types.Longitude
import org.tomohavvk.walker.protocol.Types.Speed
import org.tomohavvk.walker.protocol.Types.UnixTime
import org.tomohavvk.walker.protocol.entities.DeviceLocationEntity
import org.tomohavvk.walker.protocol.events.DeviceLocationEvent
import org.tomohavvk.walker.protocol.events.Event
import org.tomohavvk.walker.protocol.events.Metadata
import org.tomohavvk.walker.protocol.views.AcknowledgeView
import org.tomohavvk.walker.protocol.views.DeviceLocationView
import org.tomohavvk.walker.utils.ContextFlow
import org.tomohavvk.walker.utils.anySyntax
import org.tomohavvk.walker.utils.liftFSyntax
import io.scalaland.chimney.dsl._

import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.UUID

class LocationServiceImpl[F[_]: Sync: Clock, B[_]](
  repository: DeviceLocationRepository[B],
  transactor: Transactor[F, B],
  logger:     Logger[ContextFlow[F, *]])
    extends LocationService[F] {

  override def lastLocation(deviceId: DeviceId): ContextFlow[F, DeviceLocationView] = {
//    logger.info("Handle last location request") >>

    val res1: F[Option[DeviceLocationEntity]] = transactor.withTxn {
      repository.findLastById(deviceId)
    }

    val res2: ContextFlow[F, DeviceLocationView] = res1.flatMap {
      case Some(value) =>
        Applicative[F].pure(
          value
            .into[DeviceLocationView]
            .withFieldComputed(_.bearing, _.bearing.some)
            .withFieldComputed(_.altitudeAccuracy, _.altitudeAccuracy.some)
            .transform
        )
      case None => MonadError[F, Throwable].raiseError[DeviceLocationView](new RuntimeException("ENTITY NOT FOUND"))
    }.liftFlow
//      DeviceLocationView(
//        deviceId = DeviceId(UUID.randomUUID().toString),
//        latitude = Latitude(48),
//        longitude = Longitude(38),
//        accuracy = Accuracy(38),
//        altitude = Altitude(38),
//        speed = Speed(38),
//        time = UnixTime(LocalDateTime.now().toEpochSecond(ZoneOffset.UTC)),
//        bearing = Some(Bearing(180)),
//        altitudeAccuracy = Some(AltitudeAccuracy(180))
//      ).rightT
    res2
  }
}
