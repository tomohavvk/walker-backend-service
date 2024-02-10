package org.tomohavvk.walker.http.endpoints.bodies

import org.tomohavvk.walker.http.endpoints.bodies.examples.ProbesExamples
import org.tomohavvk.walker.protocol.views.ProbeView
import sttp.tapir.Codec.JsonCodec
import sttp.tapir.EndpointIO.Body
import sttp.tapir.customCodecJsonBody

trait ProbeBodies extends ProbesExamples {

  protected def probeViewBody(implicit codec: JsonCodec[ProbeView]): Body[String, ProbeView] =
    customCodecJsonBody[ProbeView].example(probeViewExample)
}
