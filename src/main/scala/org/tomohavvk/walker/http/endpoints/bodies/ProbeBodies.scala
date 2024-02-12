package org.tomohavvk.walker.http.endpoints.bodies

import org.tomohavvk.walker.protocol.views.ProbeView
import sttp.tapir.Codec.JsonCodec
import sttp.tapir.EndpointIO.Body
import sttp.tapir.customCodecJsonBody

trait ProbeBodies extends ProbesExamples {

  protected def probeViewBody(implicit codec: JsonCodec[ProbeView]): Body[String, ProbeView] =
    customCodecJsonBody[ProbeView].example(probeViewExample)
}

trait ProbesExamples {

  protected val probeViewExample: ProbeView =
    ProbeView(
      serviceName = "walker-backend-service",
      description = "walker-backend-service",
      serviceVersion = "0.1.0",
      scalaVersion = "2.12.11",
      sbtVersion = "1.5.5"
    )
}
