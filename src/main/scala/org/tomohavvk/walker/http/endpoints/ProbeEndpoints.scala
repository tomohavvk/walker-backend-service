package org.tomohavvk.walker.http.endpoints

import org.tomohavvk.walker.http.endpoints.bodies.ProbeBodies
import org.tomohavvk.walker.http.endpoints.codecs.ErrorCodecs
import org.tomohavvk.walker.http.endpoints.codecs.ProbeCodecs
import org.tomohavvk.walker.http.endpoints.mappings.ErrorMappings
import org.tomohavvk.walker.protocol.error.views.ProbeView
import sttp.model.StatusCode
import sttp.tapir._

class ProbeEndpoints(implicit probeCodecs: ProbeCodecs, val errorCodecs: ErrorCodecs)
    extends ErrorMappings
    with ProbeBodies {
  import probeCodecs._

  private val probesEndpoint =
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

}
