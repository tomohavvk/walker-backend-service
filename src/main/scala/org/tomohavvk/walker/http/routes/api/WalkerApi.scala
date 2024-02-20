package org.tomohavvk.walker.http.routes.api

import cats.Applicative
import cats.Functor
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
import org.tomohavvk.walker.protocol.errors.AppError
import org.tomohavvk.walker.protocol.views.ProbeView
import org.tomohavvk.walker.utils.UnliftF
import sttp.model.StatusCode.Ok
import sttp.tapir.server.http4s.Http4sServerOptions

class WalkerApi[F[_]: Functor: Applicative, H[_]: Async](
  endpoints:              WalkerEndpoints
)(implicit serverOptions: Http4sServerOptions[H],
  U:                      UnliftF[F, H, AppError],
  loggerH:                Logger[H]) {

  private val healthProbe: HttpRoutes[H] =
    endpoints.livenessEndpoint.toRoutes(_ => Applicative[F].pure((Ok, probeView)))

  private val readyProbe: HttpRoutes[H] =
    endpoints.readinessEndpoint.toRoutes(_ => Applicative[F].pure((Ok, probeView)))

  private val probeView: ProbeView = ProbeView(name, "walker backend service", version, scalaVersion, sbtVersion)

  val routes = healthProbe <+> readyProbe
}
