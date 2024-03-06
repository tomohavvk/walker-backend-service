package org.tomohavvk.walker.handlers

import cats.Functor
import cats.implicits.toFunctorOps
import org.tomohavvk.walker.protocol.Types.DeviceId
import org.tomohavvk.walker.protocol.ws._
import org.tomohavvk.walker.services.DevicesGroupService
import org.tomohavvk.walker.services.GroupService
import org.tomohavvk.walker.services.LocationService

trait WalkerWSMessageHandler[F[_]] {
  def handle(deviceId: DeviceId, message: WSMessageIn): F[WSMessageOut]
}

class WalkerWSMessageHandlerImpl[F[_]: Functor](
  locationService:    LocationService[F],
  groupService:       GroupService[F],
  deviceGroupService: DevicesGroupService[F])
    extends WalkerWSMessageHandler[F] {

  override def handle(deviceId: DeviceId, message: WSMessageIn): F[WSMessageOut] =
    message match {
      case LocationPersist(locations) =>
        locationService
          .upsertBatch(deviceId, locations)
          .as(LocationPersisted())

      case GroupJoin(groupId) =>
        deviceGroupService
          .joinGroup(deviceId, groupId)
          .map(_.asView)
          .map(GroupJoined)

      case create: GroupCreate =>
        groupService
          .createGroup(deviceId, create)
          .map(_.asView)
          .map(GroupCreated)

      case GroupsGet(limit, offset) =>
        groupService
          .getAllDeviceOwnedOrJoinedGroups(deviceId, limit, offset)
          .map(_.map(_.asView))
          .map(GroupsGot)

      case GroupsSearch(search, limit, offset) =>
        groupService
          .searchGroups(deviceId, search, limit, offset)
          .map(_.map(_.asView))
          .map(GroupsSearched)

      case PublicIdAvailabilityCheck(publicId) =>
        groupService
          .isPublicIdAvailable(publicId)
          .map(PublicIdAvailabilityChecked)
    }
}
