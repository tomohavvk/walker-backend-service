package org.tomohavvk.walker.http.endpoints.mappings

import org.tomohavvk.walker.http.endpoints.EndpointError
import org.tomohavvk.walker.http.endpoints.bodies.ErrorBodies
import org.tomohavvk.walker.http.endpoints.codecs.ErrorCodecs
import sttp.model.StatusCode
import sttp.tapir.EndpointOutput.OneOfVariant
import sttp.tapir.statusCode

trait ErrorMappings extends MappingHelper with ErrorBodies {
  implicit val errorCodecs: ErrorCodecs

  import errorCodecs._

  protected val badRequestStatusMapping: OneOfVariant[EndpointError] =
    statusMapping(StatusCode.BadRequest, statusCode and badRequestBody)

  protected val notFoundErrorErrorStatusMapping: OneOfVariant[EndpointError] =
    statusMapping(StatusCode.NotFound, statusCode and internalErrorBody)

  protected val internalErrorStatusMapping: OneOfVariant[EndpointError] =
    statusMapping(StatusCode.InternalServerError, statusCode and internalErrorBody)
}
