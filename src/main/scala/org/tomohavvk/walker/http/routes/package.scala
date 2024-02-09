package org.tomohavvk.walker.http

import cats.effect.kernel.Async
import cats.implicits.catsSyntaxApplicativeError
import cats.implicits.toFlatMapOps
import cats.implicits.toFunctorOps
import cats.syntax.applicative._
import cats.syntax.either._
import io.odin.Logger
import org.http4s.HttpRoutes
import org.tomohavvk.walker.http.endpoints.EndpointError
import org.tomohavvk.walker.protocol.error.AppError
import org.tomohavvk.walker.protocol.error.errors.InternalError
import org.tomohavvk.walker.utils.ContextFlow
import org.tomohavvk.walker.utils.LogContext
import org.tomohavvk.walker.utils.anySyntax
import sttp.model.StatusCode
import sttp.tapir.Endpoint
import sttp.tapir.server.http4s.Http4sServerInterpreter
import sttp.tapir.server.http4s.Http4sServerOptions

package object routes {

  implicit class MappedHttp4sHttpEndpoint[F[_]: Async, I, O](
    e:                      Endpoint[Unit, I, EndpointError, O, Any]
  )(implicit serverOptions: Http4sServerOptions[F],
    logger:                 Logger[ContextFlow[F, *]]) {

    def toRoutes(logic: I => ContextFlow[F, O], logContext: I => LogContext): HttpRoutes[F] =
      Http4sServerInterpreter(serverOptions).toRoutes(e.serverLogic { i =>
        withErrorHandling(logic(i), logContext(i))
      })

    def toRoutesNoInput(output: O): HttpRoutes[F] =
      Http4sServerInterpreter(serverOptions).toRoutes(e.serverLogic { _ =>
        withErrorHandling(output.rightT, LogContext.empty)
      })

    private def withErrorHandling(logic: ContextFlow[F, O], logContext: LogContext): F[Either[EndpointError, O]] =
      Either
        .catchNonFatal {
          logic
            .run(logContext)
            .value
            .flatMap {
              case Right(result) => result.asRight[EndpointError].pure
              case Left(error)   => logErrorAndMakeStatus(error, logContext)
            }
            .handleErrorWith(logErrorAndMakeStatus(_, logContext))
        }
        .valueOr(logErrorAndMakeStatus(_, logContext))

    private def matchStatusCode(code: Int): StatusCode =
      StatusCode
        .safeApply(code)
        .getOrElse(StatusCode.InternalServerError)

    private def logErrorAndMakeStatus(appError: AppError, logContext: LogContext): F[Either[EndpointError, O]] =
      logger
        .error(s"Error occurred: ${appError.apiMessage}, details: ${appError.logMessage.value}")
        .run(logContext)
        .value
        .as((matchStatusCode(appError.httpCode.value), appError).asLeft[O])

    private def logErrorAndMakeStatus(error: Throwable, logContext: LogContext): F[Either[EndpointError, O]] =
      logErrorAndMakeStatus(InternalError(error), logContext)
  }
}
