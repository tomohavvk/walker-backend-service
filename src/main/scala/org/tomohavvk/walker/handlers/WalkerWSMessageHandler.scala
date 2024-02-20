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
import org.tomohavvk.walker.protocol.views.DeviceGroupView
import org.tomohavvk.walker.protocol.views.GroupView
import org.tomohavvk.walker.protocol.ws.GroupCreate
import org.tomohavvk.walker.protocol.ws.GroupCreated
import org.tomohavvk.walker.protocol.ws.GroupJoin
import org.tomohavvk.walker.protocol.ws.GroupJoined
import org.tomohavvk.walker.protocol.ws.GroupsGet
import org.tomohavvk.walker.protocol.ws.GroupsGot
import org.tomohavvk.walker.protocol.ws.GroupsSearch
import org.tomohavvk.walker.protocol.ws.GroupsSearched
import org.tomohavvk.walker.protocol.ws.LocationPersist
import org.tomohavvk.walker.protocol.ws.LocationPersisted
import org.tomohavvk.walker.protocol.ws.WSMessageIn
import org.tomohavvk.walker.protocol.ws.WSMessageOut
import org.tomohavvk.walker.services.DevicesGroupService
import org.tomohavvk.walker.services.GroupService
import org.tomohavvk.walker.services.LocationService

import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset

trait WalkerWSMessageHandler[F[_]] {
  def handle(deviceId: DeviceId, message: WSMessageIn): F[WSMessageOut]
}

class WalkerWSMessageHandlerImpl[F[_]: Functor](
  locationService:    LocationService[F],
  groupService:       GroupService[F],
  deviceGroupService: DevicesGroupService[F]
)(implicit HF:        Handle[F, AppError])
    extends WalkerWSMessageHandler[F] {

  override def handle(deviceId: DeviceId, message: WSMessageIn): F[WSMessageOut] =
    message match {
      case LocationPersist(locations) =>
        locationService
          .upsertBatch(deviceId, makeEntities(deviceId, locations))
          .as(LocationPersisted())

      case GroupJoin(groupId) =>
        deviceGroupService
          .joinGroup(deviceId, groupId)
          .map(_.transformInto[DeviceGroupView])
          .map(GroupJoined)

      case create: GroupCreate =>
        groupService
          .createGroup(deviceId, create)
          .map(_.transformInto[GroupView])
          .map(GroupCreated)

      case GroupsGet(limit, offset) =>
        groupService
          .getAllDeviceOwnedOrJoinedGroups(deviceId, limit, offset)
          .map(_.map(_.transformInto[GroupView]))
          .map(GroupsGot)

      case GroupsSearch(search, limit, offset) =>
        groupService
          .searchGroups(deviceId, search, limit, offset)
          .map(_.map(_.transformInto[GroupView]))
          .map(GroupsSearched)
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
