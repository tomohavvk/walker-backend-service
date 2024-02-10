package org.tomohavvk.walker.http.routes.api

import cats.effect.kernel.Async
import cats.syntax.semigroupk._
import org.tomohavvk.walker.BuildInfo._
import io.odin.Logger
import org.http4s.HttpRoutes
import org.tomohavvk.walker.http.endpoints.ProbeEndpoints
import org.tomohavvk.walker.http.routes.MappedHttp4sHttpEndpoint
import org.tomohavvk.walker.http.routes.api.ProbeRoutes.probeView
import org.tomohavvk.walker.protocol.views.ProbeView
import org.tomohavvk.walker.utils.ContextFlow
import sttp.model.StatusCode.Ok
import sttp.tapir.server.http4s.Http4sServerOptions

class ProbeRoutes[F[_]: Async](
  endpoints:              ProbeEndpoints
)(implicit serverOptions: Http4sServerOptions[F],
  logger:                 Logger[ContextFlow[F, *]]) {

  val healthProbe: HttpRoutes[F] =
    endpoints.livenessEndpoint.toRoutesNoInput((Ok, probeView))

  val readyProbe: HttpRoutes[F] =
    endpoints.readinessEndpoint.toRoutesNoInput((Ok, probeView))

  val routes: HttpRoutes[F] = healthProbe <+> readyProbe

}

object ProbeRoutes {
  val probeView: ProbeView = ProbeView(name, "device data handler service", version, scalaVersion, sbtVersion)
}
