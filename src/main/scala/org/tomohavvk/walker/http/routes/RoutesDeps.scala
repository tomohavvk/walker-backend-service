package org.tomohavvk.walker.http.routes

import cats.Monad
import cats.implicits.toSemigroupKOps
import org.http4s.HttpRoutes
import org.tomohavvk.walker.http.routes.api.LocationRoutes
import org.tomohavvk.walker.http.routes.api.ProbeRoutes
import org.tomohavvk.walker.http.routes.openapi.OpenApiRoutes

case class RoutesDeps[F[_]: Monad](
  probeRoutes:    ProbeRoutes[F],
  locationRoutes: LocationRoutes[F],
  openapiRoutes:  OpenApiRoutes[F]) {

  val apiRoutes: HttpRoutes[F] =
    probeRoutes.routes <+> locationRoutes.routes <+> openapiRoutes.routes

}
