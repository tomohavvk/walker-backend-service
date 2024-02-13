package org.tomohavvk.walker.module

import cats.Applicative
import cats.effect.kernel.Async
import cats.effect.kernel.Sync
import cats.implicits.toSemigroupKOps
import io.odin.Logger
import org.http4s.HttpRoutes
import org.tomohavvk.walker.config.ServerConfig
import org.tomohavvk.walker.http.endpoints.ErrorHandling
import org.tomohavvk.walker.http.endpoints.WalkerEndpoints
import org.tomohavvk.walker.http.routes.api.WalkerApi
import org.tomohavvk.walker.http.routes.openapi.OpenApiRoutes
import org.tomohavvk.walker.http.server.HttpServer
import org.tomohavvk.walker.module.ServiceModule.ServicesDeps
import org.tomohavvk.walker.protocol.errors.AppError
import org.tomohavvk.walker.utils.UnliftF
import sttp.tapir.server.http4s.Http4sServerOptions
import sttp.tapir.server.interceptor.cors.CORSInterceptor
import sttp.tapir.server.interceptor.exception.DefaultExceptionHandler

object HttpModule {

  def make[F[_]: Applicative, H[_]: Async](
    services:   ServicesDeps[F],
    codecs:     Codecs,
    config:     ServerConfig
  )(implicit U: UnliftF[F, H, AppError],
    loggerH:    Logger[H]
  ): HttpServer[H] = {
    implicit val option: Http4sServerOptions[H] = makeOptions[H](codecs)
    val walkerEndpoints                         = new WalkerEndpoints(codecs.errorCodecs, codecs)
    val walkerApi =
      new WalkerApi[F, H](walkerEndpoints, services.deviceService, services.groupService, services.locationService)

    val openApi = new OpenApiRoutes[H](walkerEndpoints)

    val apiRoutes: HttpRoutes[H] = openApi.routes <+> walkerApi.routes

    new HttpServer(config, apiRoutes)
  }

  private def makeOptions[H[_]: Sync](codecs: Codecs): Http4sServerOptions[H] =
    Http4sServerOptions
      .customiseInterceptors[H]
      .copy(
        exceptionHandler = Some(DefaultExceptionHandler[H]),
        serverLog = Some(Http4sServerOptions.defaultServerLog),
        decodeFailureHandler = ErrorHandling.decodeFailureHandler(codecs.errorCodecs)
      )
      .options
      .appendInterceptor(CORSInterceptor.default)
}
