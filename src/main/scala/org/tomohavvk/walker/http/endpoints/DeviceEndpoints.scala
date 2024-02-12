package org.tomohavvk.walker.http.endpoints

import org.tomohavvk.walker.http.endpoints.bodies.DeviceBodies
import org.tomohavvk.walker.http.endpoints.codecs.DeviceCodecs
import org.tomohavvk.walker.http.endpoints.codecs.ErrorCodecs
import org.tomohavvk.walker.http.endpoints.mappings.ErrorMappings
import org.tomohavvk.walker.http.endpoints.metas.EmptyCommandMeta
import org.tomohavvk.walker.http.endpoints.metas.CreateDeviceCommandMeta
import org.tomohavvk.walker.http.endpoints.schemas.EndpointSchemas
import org.tomohavvk.walker.protocol.Types.DeviceId
import org.tomohavvk.walker.protocol.Types.TraceId
import sttp.model.StatusCode
import sttp.tapir._

class DeviceEndpoints(implicit deviceCodecs: DeviceCodecs, val errorCodecs: ErrorCodecs)
    extends ErrorMappings
    with DeviceBodies {
  import deviceCodecs._

  val createDeviceEndpoint =
    apiV1Endpoint
      .in("devices")
      .in(deviceIdPath.and(traceIdHeader).and(bodyForCreateDeviceCommand).mapTo[CreateDeviceCommandMeta])
      .tag("devices")
      .summary("Endpoint for create device")
      .description("Endpoint for create device")
      .post
      .errorOut(oneOf(internalErrorStatusMapping, alreadyExistsErrorStatusMapping, badRequestStatusMapping))
      .out(bodyForDeviceView)
      .out(statusCode(StatusCode.Ok))

  val getDeviceEndpoint =
    apiV1Endpoint
      .in("devices")
      .in(deviceIdPath.and(traceIdHeader).mapTo[EmptyCommandMeta])
      .tag("devices")
      .summary("Endpoint for fetch actual device entity")
      .description("Endpoint for fetch actual device entity")
      .get
      .errorOut(oneOf(internalErrorStatusMapping, notFoundErrorStatusMapping, badRequestStatusMapping))
      .out(bodyForDeviceView)
      .out(statusCode(StatusCode.Ok))

  private def deviceIdPath: EndpointInput.PathCapture[DeviceId] =
    path[DeviceId]
      .name("deviceId")
      .schema(EndpointSchemas.tapirDeviceIdSchema)
      .description("Device ID")
      .example(DeviceId("953a5959-1b0f-412a-bdab-1cbe15486a28"))

  private def traceIdHeader: EndpointIO.Header[TraceId] =
    header[TraceId]("X-Trace-Id")
      .description("A unique trace ID")
      .example(TraceId("953a5959-1b0f-412a-bdab-1cbe15486a28"))

}
