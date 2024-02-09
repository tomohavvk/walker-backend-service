package org.tomohavvk.walker.http.endpoints.codecs

import org.tomohavvk.walker.protocol.error.views.ProbeView
import sttp.tapir.Codec.JsonCodec

case class ProbeCodecs()(implicit val probesViewCodec: JsonCodec[ProbeView])
