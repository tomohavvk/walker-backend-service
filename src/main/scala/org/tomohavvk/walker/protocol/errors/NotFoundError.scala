package org.tomohavvk.walker.protocol.errors

import org.tomohavvk.walker.protocol.Types.ApiErrorMessage
import org.tomohavvk.walker.protocol.Types.ErrorCode
import org.tomohavvk.walker.protocol.Types.HttpCode
import org.tomohavvk.walker.protocol.Types.LogErrorMessage

case class NotFoundError(message: String)
    extends AppError(apiMessage = ApiErrorMessage(message),
                     code = ErrorCode("not_found_error"),
                     httpCode = HttpCode(404),
                     logMessage = LogErrorMessage(message)
    )
