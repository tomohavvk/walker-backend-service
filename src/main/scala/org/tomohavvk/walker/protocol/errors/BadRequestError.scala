package org.tomohavvk.walker.protocol.errors

import org.tomohavvk.walker.protocol.Types.ApiErrorMessage
import org.tomohavvk.walker.protocol.Types.ErrorCode
import org.tomohavvk.walker.protocol.Types.HttpCode
import org.tomohavvk.walker.protocol.Types.LogErrorMessage

case class BadRequestError(message: String, internalMessage: String = "Internal Error")
    extends AppError(apiMessage = ApiErrorMessage(message),
                     code = ErrorCode("bad_request_error"),
                     httpCode = HttpCode(400),
                     logMessage = LogErrorMessage(internalMessage)
    )
