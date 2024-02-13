package org.tomohavvk.walker.http.endpoints

import org.tomohavvk.walker.http.endpoints.bodies.DeviceBodies
import org.tomohavvk.walker.http.endpoints.bodies.GroupBodies
import org.tomohavvk.walker.http.endpoints.bodies.LocationBodies
import org.tomohavvk.walker.http.endpoints.bodies.ProbeBodies
import org.tomohavvk.walker.http.endpoints.codecs.ErrorCodecs
import org.tomohavvk.walker.http.endpoints.mappings.ErrorMappings
import org.tomohavvk.walker.http.endpoints.metas.CreateGroupCommandMeta
import org.tomohavvk.walker.http.endpoints.metas.EmptyBodyCommandMeta
import org.tomohavvk.walker.http.endpoints.metas.JoinGroupCommandMeta
import org.tomohavvk.walker.http.endpoints.metas.RegisterDeviceCommandMeta
import org.tomohavvk.walker.http.endpoints.schemas.EndpointSchemas
import org.tomohavvk.walker.module.Codecs
import org.tomohavvk.walker.protocol.Types.GroupId
import org.tomohavvk.walker.protocol.Types.XAuthDeviceId
import org.tomohavvk.walker.protocol.views.ProbeView
import sttp.model.StatusCode
import sttp.tapir._

class WalkerEndpoints(val errorCodecs: ErrorCodecs, codecs: Codecs)
    extends ErrorMappings
    with ProbeBodies
    with DeviceBodies
    with GroupBodies
    with LocationBodies {

  import codecs.commonCodecs._
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
      .in(authDeviceIdHeader.mapTo[EmptyBodyCommandMeta])
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
      .in(authDeviceIdHeader.and(bodyForRegisterDeviceCommand).mapTo[RegisterDeviceCommandMeta])
      .tag("devices")
      .summary("Endpoint for register device")
      .description("Endpoint for register device")
      .post
      .errorOut(oneOf(internalErrorStatusMapping, alreadyExistsErrorStatusMapping, badRequestStatusMapping))
      .out(bodyForDeviceView)
      .out(statusCode(StatusCode.Ok))

  val getDeviceEndpoint =
    apiV1Endpoint
      .in("devices")
      .in(authDeviceIdHeader.mapTo[EmptyBodyCommandMeta])
      .tag("devices")
      .summary("Endpoint for get device info")
      .description("Endpoint for get device info")
      .get
      .errorOut(oneOf(internalErrorStatusMapping, notFoundErrorStatusMapping, badRequestStatusMapping))
      .out(bodyForDeviceView)
      .out(statusCode(StatusCode.Ok))

  val createGroupEndpoint =
    apiV1Endpoint
      .in("groups")
      .in(authDeviceIdHeader.and(bodyForCreateGroupCommand).mapTo[CreateGroupCommandMeta])
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

  val joinGroupEndpoint =
    apiV1Endpoint
      .in("groups")
      .in(authDeviceIdHeader.and(groupIdPath).mapTo[JoinGroupCommandMeta])
      .in("join")
      .tag("groups")
      .summary("Endpoint for join group")
      .description("Endpoint for join group")
      .post
      .errorOut(
        oneOf(internalErrorStatusMapping,
              alreadyExistsErrorStatusMapping,
              notFoundErrorStatusMapping,
              badRequestStatusMapping
        )
      )
      .out(bodyForDeviceGroupView)
      .out(statusCode(StatusCode.Ok))

  private def groupIdPath: EndpointInput.PathCapture[GroupId] =
    path[GroupId]
      .name("groupId")
      .schema(EndpointSchemas.tapirGroupIdSchema)
      .description("Group ID")
      .example(GroupId("729d378c-1a64-4245-9569-2d1109dc9bdc"))

  private def authDeviceIdHeader: EndpointIO.Header[XAuthDeviceId] =
    header[XAuthDeviceId]("X-Auth-Device-Id")
      .description("Authenticated device id")
      .example(XAuthDeviceId("C471D192-6B42-47C6-89EF-2BCD49DB603D"))

}
