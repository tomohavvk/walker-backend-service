package org.tomohavvk.walker.module

import cats.effect.kernel.Async
import cats.effect.kernel.Sync
import ServiceModule.ServicesDeps
import io.odin.Logger
import org.tomohavvk.walker.http.endpoints.ErrorHandling
import org.tomohavvk.walker.http.routes.RoutesDeps
import org.tomohavvk.walker.http.routes.api.DeviceRoutes
import org.tomohavvk.walker.http.routes.api.LocationRoutes
import org.tomohavvk.walker.http.routes.api.ProbeRoutes
import org.tomohavvk.walker.http.routes.openapi.OpenApiRoutes
import org.tomohavvk.walker.utils.ContextFlow
import sttp.tapir.server.http4s.Http4sServerOptions
import sttp.tapir.server.interceptor.cors.CORSInterceptor
import sttp.tapir.server.interceptor.exception.DefaultExceptionHandler

object RoutesModule {

  def make[F[_]: Async, B[_]](
    endpoints:            Endpoints,
    services:             ServicesDeps[F]
  )(implicit environment: Environment[F, B]
  ): RoutesDeps[F] = {
    implicit val logger: Logger[ContextFlow[F, *]] = environment.contextLoggerF
    implicit val option: Http4sServerOptions[F]    = makeOptions(environment.codecs)

    RoutesDeps[F](
      new ProbeRoutes[F](endpoints.probe),
      new LocationRoutes[F](endpoints.location, services.locationService),
      new DeviceRoutes[F](endpoints.device, services.deviceService),
      new OpenApiRoutes[F](endpoints.probe, endpoints.location, endpoints.device)
    )
  }

  private def makeOptions[F[_]: Sync](codecs: Codecs): Http4sServerOptions[F] =
    Http4sServerOptions
      .customiseInterceptors[F]
      .copy(
        exceptionHandler = Some(DefaultExceptionHandler[F]),
        serverLog = Some(Http4sServerOptions.defaultServerLog),
        decodeFailureHandler = ErrorHandling.decodeFailureHandler(codecs.errorCodecs)
      )
      .options
      .appendInterceptor(CORSInterceptor.default)
}
