package org.tomohavvk.walker.http.endpoints

import org.tomohavvk.walker.http.endpoints.codecs.ErrorCodecs
import org.tomohavvk.walker.protocol.Types.HttpCode
import org.tomohavvk.walker.protocol.error.AppError
import org.tomohavvk.walker.protocol.error.errors.HttpError
import sttp.model.Header
import sttp.model.StatusCode
import sttp.tapir.Codec.JsonCodec
import sttp.tapir.DecodeResult.InvalidValue
import sttp.tapir.server.interceptor.DecodeFailureContext
import sttp.tapir.server.model.ValuedEndpointOutput
import sttp.tapir.server.interceptor.decodefailure.DefaultDecodeFailureHandler
import sttp.tapir.server.interceptor.decodefailure.DefaultDecodeFailureHandler.ValidationMessages
import sttp.tapir.DecodeResult
import sttp.tapir.EndpointIO
import sttp.tapir.EndpointInput
import sttp.tapir.customCodecJsonBody
import sttp.tapir.headers
import sttp.tapir.statusCode

import scala.annotation.tailrec

trait ErrorHandling {

  def decodeFailureHandler(implicit errorCodecs: ErrorCodecs): DefaultDecodeFailureHandler = {
    import errorCodecs._
    DefaultDecodeFailureHandler.default.copy(
      response = failureResponse,
      failureMessage = failureMessage
    )
  }

  private def failureResponse(
    c:                   StatusCode,
    hs:                  List[Header],
    m:                   String
  )(implicit errorCodec: JsonCodec[AppError]
  ): ValuedEndpointOutput[_] =
    ValuedEndpointOutput(
      statusCode.and(headers).and(customCodecJsonBody[AppError]),
      (c, hs, HttpError(m, HttpCode(c.code)))
    )

  private def failureMessage(ctx: DecodeFailureContext): String = {
    val base = failureSourceMessage(ctx.failingInput, ctx.failure)

    val detail = ctx.failure match {
      case InvalidValue(errors) if errors.nonEmpty => Some(ValidationMessages.validationErrorsMessage(errors))
      case _                                       => None
    }

    combineSourceAndDetail(base, detail)
  }

  private def combineSourceAndDetail(source: String, detail: Option[String]): String =
    detail match {
      case None    => source
      case Some(d) => s"$source ($d)"
    }

  @tailrec
  private def failureSourceMessage(input: EndpointInput[_], failure: DecodeResult.Failure): String =
    input match {
      case EndpointInput.FixedMethod(_, _, _)      => "Invalid value for: method"
      case EndpointInput.FixedPath(_, _, _)        => "Invalid value for: path segment"
      case EndpointInput.PathCapture(name, _, _)   => s"Invalid value for: path parameter ${name.getOrElse("?")}"
      case EndpointInput.PathsCapture(_, _)        => "Invalid value for: path"
      case EndpointInput.Query(name, _, _, _)      => s"Invalid value for: query parameter $name"
      case EndpointInput.QueryParams(_, _)         => "Invalid value for: query parameters"
      case EndpointInput.Cookie(name, _, _)        => s"Invalid value for: cookie $name"
      case _: EndpointInput.ExtractFromRequest[_]  => "Invalid value"
      case a: EndpointInput.Auth[_, _]             => failureSourceMessage(a.input, failure)
      case _: EndpointInput.MappedPair[_, _, _, _] => "Invalid value"
      case _: EndpointIO.Body[_, _]                => errorBodyMessage(failure)
      case _: EndpointIO.StreamBodyWrapper[_, _]   => "Invalid value for: body"
      case EndpointIO.Header(name, _, _)           => s"Invalid value for: header $name"
      case EndpointIO.FixedHeader(name, _, _)      => s"Invalid value for: header $name"
      case EndpointIO.Headers(_, _)                => "Invalid value for: headers"
      case _                                       => "Invalid value"
    }

  private def errorBodyMessage(failure: DecodeResult.Failure) =
    failure match {
      case error: sttp.tapir.DecodeResult.Error => s"Decoding error: ${error.error}"
      case _                                    => "Invalid value for: body"
    }

}

object ErrorHandling extends ErrorHandling
