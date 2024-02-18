package org.tomohavvk.walker.handlers

import cats.Functor
import cats.implicits.toFunctorOps
import cats.mtl.Handle
import io.scalaland.chimney.dsl.TransformerOps
import org.tomohavvk.walker.protocol.DeviceLocation
import org.tomohavvk.walker.protocol.Types.AltitudeAccuracy
import org.tomohavvk.walker.protocol.Types.Bearing
import org.tomohavvk.walker.protocol.Types.DeviceId
import org.tomohavvk.walker.protocol.entities.DeviceLocationEntity
import org.tomohavvk.walker.protocol.errors.AppError
import org.tomohavvk.walker.protocol.ws.{Acknowledge, PersistDeviceLocation, WSMessageIn, WSMessageOut}
import org.tomohavvk.walker.services.LocationService

import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset

trait WalkerWSMessageHandler[F[_]] {
  def handle(deviceId: DeviceId, message: WSMessageIn): F[WSMessageOut]
}

class WalkerWSMessageHandlerImpl[F[_]: Functor](
  locationService: LocationService[F]
)(implicit HF:     Handle[F, AppError])
    extends WalkerWSMessageHandler[F] {

  override def handle(deviceId: DeviceId, message: WSMessageIn): F[WSMessageOut] =
    message match {
      case PersistDeviceLocation(locations) =>
        locationService
          .upsertBatch(deviceId, makeEntities(deviceId, locations))
          .as(Acknowledge(true))
    }

  private def makeEntities(deviceId: DeviceId, locations: List[DeviceLocation]): List[DeviceLocationEntity] =
    locations
      .map { location =>
        location
          .into[DeviceLocationEntity]
          .withFieldConst(_.deviceId, deviceId)
          .withFieldComputed(_.bearing, _.bearing.getOrElse(Bearing(0)))
          .withFieldComputed(_.altitudeAccuracy, _.altitudeAccuracy.getOrElse(AltitudeAccuracy(0)))
          .withFieldComputed(_.time, l => LocalDateTime.ofInstant(Instant.ofEpochMilli(l.time.value), ZoneOffset.UTC))
          .transform
      }
}
