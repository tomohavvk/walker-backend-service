package org.tomohavvk.walker.protocol.errors

import org.tomohavvk.walker.protocol.Types.ApiErrorMessage
import org.tomohavvk.walker.protocol.Types.ErrorCode
import org.tomohavvk.walker.protocol.Types.HttpCode
import org.tomohavvk.walker.protocol.Types.LogErrorMessage

class DatabaseError(internalMessage: String, override val exception: Option[Throwable])
    extends AppError(
      apiMessage = ApiErrorMessage("Database error"),
      code = ErrorCode("database_error"),
      httpCode = HttpCode(500),
      logMessage = LogErrorMessage(internalMessage)
    )

case class UniqueConstraintError(
  message:                String,
  internalMessage:        String = "Unique constraint error",
  override val exception: Option[Throwable] = None)
    extends DatabaseError(internalMessage, exception) {
  override val apiMessage: ApiErrorMessage = ApiErrorMessage(message)
  override val code: ErrorCode             = ErrorCode("conflict_error")
  override val httpCode: HttpCode          = HttpCode(409)
}

case class ViolatesForeignKeyError(internalMessage: String, override val exception: Option[Throwable])
    extends DatabaseError(internalMessage, exception)
