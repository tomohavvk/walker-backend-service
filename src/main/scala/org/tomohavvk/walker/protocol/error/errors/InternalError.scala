package org.tomohavvk.walker.protocol.error.errors

import org.tomohavvk.walker.protocol.Types.ApiErrorMessage
import org.tomohavvk.walker.protocol.Types.ErrorCode
import org.tomohavvk.walker.protocol.Types.HttpCode
import org.tomohavvk.walker.protocol.Types.LogErrorMessage
import org.tomohavvk.walker.protocol.error.AppError

case class InternalError(message: String)
    extends AppError(apiMessage = ApiErrorMessage("Internal error"),
                     code = ErrorCode("internal_error"),
                     httpCode = HttpCode(500),
                     logMessage = LogErrorMessage(message)
    )

object InternalError {
  def apply(exception: Throwable): AppError = new InternalError(exception.getMessage)
}
