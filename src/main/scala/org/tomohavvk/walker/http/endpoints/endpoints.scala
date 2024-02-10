package org.tomohavvk.walker.http

import org.tomohavvk.walker.protocol.errors.AppError
import sttp.model.StatusCode
import sttp.tapir.Endpoint
import sttp.tapir.endpoint
import sttp.tapir._
package object endpoints {

  val apiV1Endpoint = endpoint.in("api" / "v1")
  type EndpointError      = (StatusCode, AppError)
  type BaseEndpoint[I, O] = Endpoint[Unit, I, EndpointError, O, Any]

}
