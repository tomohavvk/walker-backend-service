package org.tomohavvk.walker.http.endpoints.codecs

import org.tomohavvk.walker.protocol.views.ProbeView
import sttp.tapir.Codec.JsonCodec

case class ProbeCodecs()(implicit val probesViewCodec: JsonCodec[ProbeView])
