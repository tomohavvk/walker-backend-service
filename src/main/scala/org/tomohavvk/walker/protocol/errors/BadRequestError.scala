package org.tomohavvk.walker.protocol.errors

import org.tomohavvk.walker.protocol.Types.ApiErrorMessage
import org.tomohavvk.walker.protocol.Types.ErrorCode
import org.tomohavvk.walker.protocol.Types.HttpCode
import org.tomohavvk.walker.protocol.Types.LogErrorMessage

class BadRequestError(message: String)
    extends AppError(apiMessage = ApiErrorMessage(message),
                     code = ErrorCode("bad_request_error"),
                     httpCode = HttpCode(400),
                     logMessage = LogErrorMessage(message)
    )

case class GroupIdNotUniqueError(message: String) extends BadRequestError(message)
case class GroupPublicIdNotUniqueError(message: String) extends BadRequestError(message)

object BadRequestError {
  def apply(message: String): BadRequestError = new BadRequestError(message)
}
