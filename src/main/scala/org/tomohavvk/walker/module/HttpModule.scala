package org.tomohavvk.walker.module

import cats.effect.kernel.Async
import org.tomohavvk.walker.http.routes.RoutesDeps
import org.tomohavvk.walker.http.server.HttpServer

object HttpModule {

  def make[F[_]: Async, B[_]](
    routes:               RoutesDeps[F]
  )(implicit environment: Environment[F, B]
  ): HttpServer[F] =
    new HttpServer(environment.configs.server, routes)
}
