package org.tomohavvk.walker.http.routes.api

import cats.Applicative
import cats.effect.kernel.Async
import cats.implicits.toSemigroupKOps
import io.odin.Logger
import org.http4s.HttpRoutes
import org.tomohavvk.walker.BuildInfo.name
import org.tomohavvk.walker.BuildInfo.sbtVersion
import org.tomohavvk.walker.BuildInfo.scalaVersion
import org.tomohavvk.walker.BuildInfo.version
import org.tomohavvk.walker.http.endpoints.WalkerEndpoints
import org.tomohavvk.walker.http.routes.MappedHttp4sHttpEndpoint
import org.tomohavvk.walker.protocol.Types.DeviceId
import org.tomohavvk.walker.protocol.errors.AppError
import org.tomohavvk.walker.protocol.views.ProbeView
import org.tomohavvk.walker.services.DeviceService
import org.tomohavvk.walker.services.DevicesGroupService
import org.tomohavvk.walker.services.GroupService
import org.tomohavvk.walker.services.LocationService
import org.tomohavvk.walker.utils.UnliftF
import sttp.model.StatusCode.Ok
import sttp.tapir.server.http4s.Http4sServerOptions

class WalkerApi[F[_]: Applicative, H[_]: Async](
  endpoints:              WalkerEndpoints,
  deviceService:          DeviceService[F],
  groupService:           GroupService[F],
  devicesGroupService:    DevicesGroupService[F],
  locationService:        LocationService[F]
)(implicit serverOptions: Http4sServerOptions[H],
  U:                      UnliftF[F, H, AppError],
  loggerH:                Logger[H]) {

  private val healthProbe: HttpRoutes[H] =
    endpoints.livenessEndpoint.toRoutes(_ => Applicative[F].pure((Ok, probeView)))

  private val readyProbe: HttpRoutes[H] =
    endpoints.readinessEndpoint.toRoutes(_ => Applicative[F].pure((Ok, probeView)))

  private val handleDeviceLocationRoute: HttpRoutes[H] =
    endpoints.getLatestDeviceLocationEndpoint.toRoutes(meta =>
      locationService.lastLocation(DeviceId(meta.authenticatedDeviceId.value))
    )

  private val getDeviceRoute: HttpRoutes[H] =
    endpoints.getDeviceEndpoint.toRoutes(meta => deviceService.getDevice(DeviceId(meta.authenticatedDeviceId.value)))

  private val createDeviceRoute: HttpRoutes[H] =
    endpoints.createDeviceEndpoint.toRoutes(meta =>
      deviceService.register(DeviceId(meta.authenticatedDeviceId.value), meta.command)
    )

  private val createGroupRoute: HttpRoutes[H] =
    endpoints.createGroupEndpoint.toRoutes(meta =>
      groupService.createGroup(DeviceId(meta.authenticatedDeviceId.value), meta.command)
    )

  private val getAllDeviceGroupRoute: HttpRoutes[H] =
    endpoints.getAllDeviceGroupEndpoint.toRoutes(meta =>
      groupService.getAllDeviceOwnedOrJoinedGroups(DeviceId(meta.authenticatedDeviceId.value))
    )

  private val joinGroupRoute: HttpRoutes[H] =
    endpoints.joinGroupEndpoint.toRoutes(meta =>
      devicesGroupService.joinGroup(DeviceId(meta.authenticatedDeviceId.value), meta.groupId)
    )

  private val probeView: ProbeView = ProbeView(name, "walker backend service", version, scalaVersion, sbtVersion)

  val routes =
    healthProbe <+> readyProbe <+> handleDeviceLocationRoute <+> getDeviceRoute <+> createDeviceRoute <+> createGroupRoute <+> joinGroupRoute <+> getAllDeviceGroupRoute
}
