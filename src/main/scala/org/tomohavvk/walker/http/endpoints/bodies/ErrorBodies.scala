package org.tomohavvk.walker.http.endpoints.bodies

import org.tomohavvk.walker.protocol.errors.UniqueConstraintError
import org.tomohavvk.walker.protocol.errors.AppError
import org.tomohavvk.walker.protocol.errors.BadRequestError
import org.tomohavvk.walker.protocol.errors.InternalError
import org.tomohavvk.walker.protocol.errors.NotFoundError
import sttp.tapir.Codec.JsonCodec
import sttp.tapir.EndpointIO.Body
import sttp.tapir.customCodecJsonBody

trait ErrorBodies extends ErrorExamples {

  protected def badRequestBody(implicit codec: JsonCodec[AppError]): Body[String, AppError] =
    errorOutput("Bad request").example(badRequestErrorExample)

  protected def notFoundBody(implicit codec: JsonCodec[AppError]): Body[String, AppError] =
    errorOutput("The requested resource was not found").example(notFoundErrorExample)

  protected def alreadyExistsBody(implicit codec: JsonCodec[AppError]): Body[String, AppError] =
    errorOutput("The entity to create already exists").example(alreadyExistsErrorExample)

  protected def internalErrorBody(implicit codec: JsonCodec[AppError]): Body[String, AppError] =
    errorOutput("There is an internal server error").example(internalErrorExample)

  protected def errorOutput(
    description:    String
  )(implicit codec: JsonCodec[AppError]
  ): Body[String, AppError] =
    customCodecJsonBody[AppError].description(description)

}

trait ErrorExamples {

  protected val internalErrorExample: AppError = InternalError("Something went wrong")

  protected val notFoundErrorExample: AppError = NotFoundError("The requested entity can't be found")

  protected val badRequestErrorExample: AppError =
    BadRequestError("Internal error", "The request contains bad syntax or cannot be fulfilled.")

  protected val alreadyExistsErrorExample: AppError =
    UniqueConstraintError("Entity already exists")

}
