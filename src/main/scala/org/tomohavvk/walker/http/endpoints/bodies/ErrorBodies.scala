package org.tomohavvk.walker.http.endpoints.bodies

import org.tomohavvk.walker.http.endpoints.bodies.examples.ErrorExamples
import org.tomohavvk.walker.protocol.error.AppError
import sttp.tapir.Codec.JsonCodec
import sttp.tapir.EndpointIO.Body
import sttp.tapir.customCodecJsonBody

trait ErrorBodies extends ErrorExamples {

  protected def badRequestBody(implicit codec: JsonCodec[AppError]): Body[String, AppError] =
    errorOutput("Bad request").example(badRequestErrorExample)

  protected def notFoundBody(implicit codec: JsonCodec[AppError]): Body[String, AppError] =
    errorOutput("The requested resource was not found").example(notFoundErrorExample)

  protected def internalErrorBody(implicit codec: JsonCodec[AppError]): Body[String, AppError] =
    errorOutput("There is an internal server error").example(internalErrorExample)

  protected def errorOutput(
    description:    String
  )(implicit codec: JsonCodec[AppError]
  ): Body[String, AppError] =
    customCodecJsonBody[AppError].description(description)

}
