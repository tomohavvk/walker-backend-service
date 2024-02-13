package org.tomohavvk.walker.http.routes.openapi

import cats.effect.kernel.Async
import org.http4s.HttpRoutes
import org.tomohavvk.walker.BuildInfo
import org.tomohavvk.walker.http.endpoints.DeviceEndpoints
import org.tomohavvk.walker.http.endpoints.LocationEndpoints
import org.tomohavvk.walker.http.endpoints.ProbeEndpoints
import sttp.apispec.openapi.Info
import sttp.apispec.openapi.circe.yaml.RichOpenAPI
import sttp.tapir.docs.openapi.OpenAPIDocsInterpreter
import sttp.tapir.server.http4s.Http4sServerInterpreter
import sttp.tapir.swagger.SwaggerUI

class OpenApiRoutes[H[_]: Async](
  probeEndpoints:    ProbeEndpoints,
  locationEndpoints: LocationEndpoints,
  deviceEndpoints:   DeviceEndpoints) {

  val endpoints = List(
    probeEndpoints.livenessEndpoint,
    probeEndpoints.readinessEndpoint,
    locationEndpoints.getLatestDeviceLocationEndpoint,
    deviceEndpoints.getDeviceEndpoint,
    deviceEndpoints.createDeviceEndpoint
  )

  val openApiInfo: Info = Info(
    title = BuildInfo.name,
    version = BuildInfo.version
  )

  val openApiYaml: String = OpenAPIDocsInterpreter().toOpenAPI(endpoints, openApiInfo).toYaml

  val routes: HttpRoutes[H] = Http4sServerInterpreter[H]().toRoutes(SwaggerUI[H](openApiYaml))

}
