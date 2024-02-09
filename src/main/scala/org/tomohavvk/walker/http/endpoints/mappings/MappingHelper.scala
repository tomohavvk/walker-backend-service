package org.tomohavvk.walker.http.endpoints.mappings

import sttp.model.StatusCode
import sttp.tapir.EndpointOutput.OneOfVariant
import sttp.tapir.EndpointOutput
import sttp.tapir.oneOfVariantValueMatcher

trait MappingHelper {

  def statusMapping[T](
    statusCode: StatusCode,
    output:     EndpointOutput[T]
  ): OneOfVariant[T] =
    oneOfVariantValueMatcher(statusCode, output) {
      case (code, _) if code == statusCode => true
    }
}
