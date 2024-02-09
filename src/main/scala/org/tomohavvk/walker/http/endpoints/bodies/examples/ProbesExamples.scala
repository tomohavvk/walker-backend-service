package org.tomohavvk.walker.http.endpoints.bodies.examples

import org.tomohavvk.walker.protocol.error.views.ProbeView

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
