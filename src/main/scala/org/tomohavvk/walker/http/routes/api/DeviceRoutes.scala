package org.tomohavvk.walker.http.routes.api

import cats.effect.kernel.Async
import cats.implicits.catsSyntaxOptionId
import cats.implicits.toSemigroupKOps
import io.odin.Logger
import org.http4s.HttpRoutes
import org.tomohavvk.walker.http.endpoints.DeviceEndpoints
import org.tomohavvk.walker.http.routes.MappedHttp4sHttpEndpoint
import org.tomohavvk.walker.services.DeviceService
import org.tomohavvk.walker.utils.ContextFlow
import org.tomohavvk.walker.utils.LogContext
import sttp.tapir.server.http4s.Http4sServerOptions

class DeviceRoutes[F[_]: Async](
  endpoints:              DeviceEndpoints,
  service:                DeviceService[F]
)(implicit serverOptions: Http4sServerOptions[F],
  logger:                 Logger[ContextFlow[F, *]]) {

  private val getDeviceRoute: HttpRoutes[F] =
    endpoints.getDeviceEndpoint.toRoutes(
      meta => service.findDevice(meta.deviceId),
      meta => LogContext(meta.traceId.some, meta.deviceId.some)
    )

  private val createDeviceRoute: HttpRoutes[F] =
    endpoints.createDeviceEndpoint.toRoutes(
      meta => service.createDevice(meta.deviceId, meta.command),
      meta => LogContext(meta.traceId.some, meta.deviceId.some)
    )

  val routes: HttpRoutes[F] = getDeviceRoute <+> createDeviceRoute

}
