package org.tomohavvk.walker.http.endpoints.codecs

import org.tomohavvk.walker.protocol.error.AppError
import sttp.tapir.Codec.JsonCodec

case class ErrorCodecs()(implicit val errorCodec: JsonCodec[AppError])
