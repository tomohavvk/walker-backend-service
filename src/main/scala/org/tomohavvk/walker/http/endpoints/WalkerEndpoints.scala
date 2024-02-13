package org.tomohavvk.walker.http.endpoints

import org.tomohavvk.walker.http.endpoints.bodies.DeviceBodies
import org.tomohavvk.walker.http.endpoints.bodies.GroupBodies
import org.tomohavvk.walker.http.endpoints.bodies.LocationBodies
import org.tomohavvk.walker.http.endpoints.bodies.ProbeBodies
import org.tomohavvk.walker.http.endpoints.codecs.ErrorCodecs
import org.tomohavvk.walker.http.endpoints.mappings.ErrorMappings
import org.tomohavvk.walker.http.endpoints.metas.CreateDeviceCommandMeta
import org.tomohavvk.walker.http.endpoints.metas.CreateGroupCommandMeta
import org.tomohavvk.walker.http.endpoints.metas.EmptyCommandMeta
import org.tomohavvk.walker.http.endpoints.schemas.EndpointSchemas
import org.tomohavvk.walker.module.Codecs
import org.tomohavvk.walker.protocol.Types.DeviceId
import org.tomohavvk.walker.protocol.Types.TraceId
import org.tomohavvk.walker.protocol.views.ProbeView
import sttp.model.StatusCode
import sttp.tapir.endpoint
import sttp.tapir.oneOf
import sttp.tapir.statusCode
import sttp.tapir._

class WalkerEndpoints(val errorCodecs: ErrorCodecs, codecs: Codecs)
    extends ErrorMappings
    with ProbeBodies
    with DeviceBodies
    with GroupBodies
    with LocationBodies {

  import codecs.deviceCodecs._
  import codecs.groupCodecs._
  import codecs.locationCodecs._
  import codecs.probe._

  val probesEndpoint =
    endpoint
      .in("probes")
      .tag("probes")
      .errorOut(oneOf(internalErrorStatusMapping))
      .out(statusCode)
      .out(probeViewBody)

  val livenessEndpoint: BaseEndpoint[Unit, (StatusCode, ProbeView)] =
    probesEndpoint.get
      .summary("Liveness probe")
      .description("Detect that the service is up")
      .in("liveness")

  val readinessEndpoint: BaseEndpoint[Unit, (StatusCode, ProbeView)] =
    probesEndpoint.get
      .summary("Readiness probe")
      .description("Detect that the service is ready")
      .in("readiness")

  val getLatestDeviceLocationEndpoint =
    apiV1Endpoint
      .in("devices")
      .in(deviceIdPath.and(traceIdHeader).mapTo[EmptyCommandMeta])
      .in("location")
      .tag("location")
      .summary("Endpoint for fetch latest device location")
      .description("Endpoint for fetch latest device location")
      .get
      .errorOut(oneOf(internalErrorStatusMapping, notFoundErrorStatusMapping, badRequestStatusMapping))
      .out(bodyForDeviceLocationView)
      .out(statusCode(StatusCode.Ok))

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

  val createGroupEndpoint =
    apiV1Endpoint
      .in("groups")
      .in(traceIdHeader.and(bodyForCreateGroupCommand).mapTo[CreateGroupCommandMeta])
      .tag("groups")
      .summary("Endpoint for create group")
      .description("Endpoint for create group")
      .post
      .errorOut(
        oneOf(internalErrorStatusMapping,
              alreadyExistsErrorStatusMapping,
              notFoundErrorStatusMapping,
              badRequestStatusMapping
        )
      )
      .out(bodyForGroupView)
      .out(statusCode(StatusCode.Ok))

  private def deviceIdPath: EndpointInput.PathCapture[DeviceId] =
    path[DeviceId]
      .name("deviceId")
      .schema(EndpointSchemas.tapirDeviceIdSchema)
      .description("Device ID")
      .example(DeviceId("C471D192-6B42-47C6-89EF-2BCD49DB603D"))

  private def groupIdPath: EndpointInput.PathCapture[DeviceId] =
    path[DeviceId]
      .name("groupId")
      .schema(EndpointSchemas.tapirDeviceIdSchema)
      .description("Group ID")
      .example(DeviceId("953a5959-1b0f-412a-bdab-1cbe15486a28"))

  private def traceIdHeader: EndpointIO.Header[TraceId] =
    header[TraceId]("X-Trace-Id")
      .description("A unique trace ID")
      .example(TraceId("953a5959-1b0f-412a-bdab-1cbe15486a28"))

}
