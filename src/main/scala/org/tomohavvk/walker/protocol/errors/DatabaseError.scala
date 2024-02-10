package org.tomohavvk.walker.protocol.errors

import org.tomohavvk.walker.protocol.Types.ApiErrorMessage
import org.tomohavvk.walker.protocol.Types.ErrorCode
import org.tomohavvk.walker.protocol.Types.HttpCode
import org.tomohavvk.walker.protocol.Types.LogErrorMessage

case class DatabaseError(internalMessage: String, override val exception: Option[Throwable])
    extends AppError(
      apiMessage = ApiErrorMessage("Database error"),
      code = ErrorCode("database_error"),
      httpCode = HttpCode(500),
      logMessage = LogErrorMessage(internalMessage)
    )
