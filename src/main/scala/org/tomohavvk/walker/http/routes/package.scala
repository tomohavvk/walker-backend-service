package org.tomohavvk.walker.http

import cats.Applicative
import cats.effect.kernel.Async
import cats.implicits.toFlatMapOps
import cats.implicits.toFunctorOps
import cats.syntax.either._
import io.odin.Logger
import org.http4s.HttpRoutes
import org.tomohavvk.walker.http.endpoints.EndpointError
import org.tomohavvk.walker.protocol.errors.AppError
import org.tomohavvk.walker.utils.UnliftF
import sttp.model.StatusCode
import sttp.tapir.Endpoint
import sttp.tapir.server.http4s.Http4sServerInterpreter
import sttp.tapir.server.http4s.Http4sServerOptions

package object routes {

  implicit class MappedHttp4sHttpEndpoint[I, O](
    e: Endpoint[Unit, I, EndpointError, O, Any]) {

    def toRoutes[F[_], H[_]: Async](
      logic:                  I => F[O]
    )(implicit serverOptions: Http4sServerOptions[H],
      U:                      UnliftF[F, H, AppError],
      loggerH:                Logger[H]
    ): HttpRoutes[H] = {

      def unlifted(i: I): H[Either[AppError, O]] =
        U.unlift[O](logic(i))

      Http4sServerInterpreter(serverOptions).toRoutes(e.serverLogic { i =>
        unlifted(i)
          .map {
            case Left(error) =>
              (StatusCode.unsafeApply(error.httpCode.value) -> error).asLeft
            case Right(ok) =>
              ok.asRight
          }
          .flatMap[Either[(StatusCode, AppError), O]] {
            case v @ Right(_) => Applicative[H].pure(v)
            case e @ Left((_, error)) =>
              loggerH.error(error.logMessage.value).as(e)
          }
      })
    }
  }
}
