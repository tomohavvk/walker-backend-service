package org.tomohavvk.walker.module

import cats.effect.kernel.Async
import cats.effect.kernel.Ref
import cats.effect.kernel.Sync
import cats.implicits.toFunctorOps
import cats.implicits.toSemigroupKOps
import cats.Applicative
import cats.~>
import fs2.concurrent.Topic
import io.odin.Logger
import org.http4s.HttpRoutes
import org.http4s.websocket.WebSocketFrame
import org.tomohavvk.walker.config.ServerConfig
import org.tomohavvk.walker.handlers.WalkerWSMessageHandlerImpl
import org.tomohavvk.walker.http.endpoints.ErrorHandling
import org.tomohavvk.walker.http.endpoints.WalkerEndpoints
import org.tomohavvk.walker.http.routes.api.WalkerApi
import org.tomohavvk.walker.http.routes.api.WalkerWSApi
import org.tomohavvk.walker.http.routes.api.WalkerWSApi.WSSubscribers
import org.tomohavvk.walker.http.routes.openapi.OpenApiRoutes
import org.tomohavvk.walker.http.server.HttpServer
import org.tomohavvk.walker.module.ServiceModule.ServicesDeps
import org.tomohavvk.walker.protocol.Types.DeviceId
import org.tomohavvk.walker.protocol.errors.AppError
import org.tomohavvk.walker.utils.UnliftF
import sttp.tapir.server.http4s.Http4sServerOptions
import sttp.tapir.server.interceptor.cors.CORSInterceptor
import sttp.tapir.server.interceptor.exception.DefaultExceptionHandler

object HttpModule {

  def make[F[_], M[_]: Async](
    services:   ServicesDeps[F],
    codecs:     Codecs,
    config:     ServerConfig
  )(implicit U: UnliftF[F, M, AppError],
    LiftHF:     M ~> F,
    A:          Applicative[F],
    loggerM:    Logger[M]
  ): F[HttpServer[F, M]] = {
    implicit val option: Http4sServerOptions[M] = makeOptions[M](codecs)
    val walkerEndpoints                         = new WalkerEndpoints(codecs.errorCodecs, codecs)

    val walkerApi =
      new WalkerApi[F, M](walkerEndpoints)

    val wsMessageHandler =
      new WalkerWSMessageHandlerImpl[F](services.locationService, services.groupService, services.devicesGroupService)

    val openApi = new OpenApiRoutes[M](walkerEndpoints)

    val apiRoutes: HttpRoutes[M] = openApi.routes <+> walkerApi.routes

    LiftHF(Ref.of[M, Map[DeviceId, Topic[M, WebSocketFrame]]](Map.empty))
      .map { subscriptionRef =>
        val walkerWsApi =
          new WalkerWSApi[F, M](services.deviceService, wsMessageHandler, WSSubscribers[M](subscriptionRef), loggerM)

        new HttpServer(config, apiRoutes, walkerWsApi)
      }
  }

  private def makeOptions[M[_]: Sync](codecs: Codecs): Http4sServerOptions[M] =
    Http4sServerOptions
      .customiseInterceptors[M]
      .copy(
        exceptionHandler = Some(DefaultExceptionHandler[M]),
        serverLog = Some(Http4sServerOptions.defaultServerLog),
        decodeFailureHandler = ErrorHandling[M]().decodeFailureHandler(codecs.errorCodecs)
      )
      .options
      .appendInterceptor(CORSInterceptor.default)
}
