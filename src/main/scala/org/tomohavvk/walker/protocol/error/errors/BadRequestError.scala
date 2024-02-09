package org.tomohavvk.walker.protocol.error.errors

import org.tomohavvk.walker.protocol.Types._
import org.tomohavvk.walker.protocol.error.AppError

case class BadRequestError(internalMessage: String, message: String = "Internal Error")
    extends AppError(apiMessage = ApiErrorMessage(message),
                     code = ErrorCode("bad_request_error"),
                     httpCode = HttpCode(400),
                     logMessage = LogErrorMessage(internalMessage)
    )
