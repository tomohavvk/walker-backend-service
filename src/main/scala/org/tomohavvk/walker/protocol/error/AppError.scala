package org.tomohavvk.walker.protocol.error

import org.tomohavvk.walker.protocol.Types.ApiErrorMessage
import org.tomohavvk.walker.protocol.Types.ErrorCode
import org.tomohavvk.walker.protocol.Types.HttpCode
import org.tomohavvk.walker.protocol.Types.LogErrorMessage

abstract class AppError(
  val apiMessage: ApiErrorMessage,
  val code:       ErrorCode,
  val logMessage: LogErrorMessage,
  val httpCode:   HttpCode,
  val exception:  Option[Throwable] = Option.empty)
