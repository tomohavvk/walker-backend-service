package org.tomohavvk.walker.utils

import io.odin.loggers.HasContext
import org.tomohavvk.walker.protocol.Types.DeviceId
import org.tomohavvk.walker.protocol.Types.TraceId

case class LogContext(traceId: Option[TraceId] = None, deviceId: Option[DeviceId] = None)

object LogContext {

  implicit val hasContext: HasContext[LogContext] = context =>
    List(
      context.traceId.map(traceId => "trace_id" -> traceId.value),
      context.deviceId.map(deviceId => "device_id" -> deviceId.value)
    ).flatten.toMap

  def empty: LogContext = LogContext(None)

}
