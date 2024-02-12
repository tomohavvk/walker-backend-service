package org.tomohavvk.walker.http.endpoints.metas

import org.tomohavvk.walker.protocol.Types.DeviceId
import org.tomohavvk.walker.protocol.Types.TraceId

case class EmptyCommandMeta(deviceId: DeviceId, traceId: TraceId)
