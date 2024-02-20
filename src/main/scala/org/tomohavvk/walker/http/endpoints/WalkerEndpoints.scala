package org.tomohavvk.walker.http.endpoints

import org.tomohavvk.walker.http.endpoints.bodies.ProbeBodies
import org.tomohavvk.walker.http.endpoints.codecs.ErrorCodecs
import org.tomohavvk.walker.http.endpoints.mappings.ErrorMappings
import org.tomohavvk.walker.module.Codecs
import org.tomohavvk.walker.protocol.errors.AppError
import org.tomohavvk.walker.protocol.views.ProbeView
import sttp.model.StatusCode
import sttp.tapir._

class WalkerEndpoints(val errorCodecs: ErrorCodecs, codecs: Codecs) extends ErrorMappings with ProbeBodies {

  import codecs.probe._

  val probesEndpoint: Endpoint[Unit, Unit, (StatusCode, AppError), (StatusCode, ProbeView), Any] =
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
