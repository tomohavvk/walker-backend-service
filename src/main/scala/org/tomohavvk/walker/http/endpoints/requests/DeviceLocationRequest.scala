package org.tomohavvk.walker.http.endpoints.requests

import org.tomohavvk.walker.protocol.Types.DeviceId
import org.tomohavvk.walker.protocol.Types.TraceId

case class DeviceLocationRequest(deviceId: DeviceId, traceId: TraceId)
