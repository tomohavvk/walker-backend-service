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
import org.tomohavvk.walker.http.endpoints.DeviceEndpoints
import org.tomohavvk.walker.http.endpoints.LocationEndpoints
import org.tomohavvk.walker.http.endpoints.ProbeEndpoints
import org.tomohavvk.walker.http.routes.MappedHttp4sHttpEndpoint
import org.tomohavvk.walker.protocol.errors.AppError
import org.tomohavvk.walker.protocol.views.ProbeView
import org.tomohavvk.walker.services.DeviceService
import org.tomohavvk.walker.services.LocationService
import org.tomohavvk.walker.utils.UnliftF
import sttp.model.StatusCode.Ok
import sttp.tapir.server.http4s.Http4sServerOptions

class WalkerApi[F[_]: Applicative, H[_]: Async](
  deviceEndpoints:        DeviceEndpoints,
  locationEndpoints:      LocationEndpoints,
  probesEndpoints:        ProbeEndpoints,
  deviceService:          DeviceService[F],
  locationService:        LocationService[F]
)(implicit serverOptions: Http4sServerOptions[H],
  U:                      UnliftF[F, H, AppError],
  loggerH:                Logger[H]) {

  private val healthProbe: HttpRoutes[H] =
    probesEndpoints.livenessEndpoint.toRoutes(_ => Applicative[F].pure((Ok, probeView)))

  private val readyProbe: HttpRoutes[H] =
    probesEndpoints.readinessEndpoint.toRoutes(_ => Applicative[F].pure((Ok, probeView)))

  private val handleDeviceLocationRoute: HttpRoutes[H] =
    locationEndpoints.getLatestDeviceLocationEndpoint.toRoutes(meta => locationService.lastLocation(meta.deviceId))

  private val getDeviceRoute: HttpRoutes[H] =
    deviceEndpoints.getDeviceEndpoint.toRoutes(meta => deviceService.findDevice(meta.deviceId))

  private val createDeviceRoute: HttpRoutes[H] =
    deviceEndpoints.createDeviceEndpoint.toRoutes(meta => deviceService.createDevice(meta.deviceId, meta.command))

  private val probeView: ProbeView = ProbeView(name, "walker backend service", version, scalaVersion, sbtVersion)

  val routes = healthProbe <+> readyProbe <+> handleDeviceLocationRoute <+> getDeviceRoute <+> createDeviceRoute
}
