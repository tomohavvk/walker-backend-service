package org.tomohavvk.walker.http.routes.openapi

import cats.effect.kernel.Async
import org.http4s.HttpRoutes
import org.tomohavvk.walker.BuildInfo
import org.tomohavvk.walker.http.endpoints.WalkerEndpoints
import sttp.apispec.openapi.Info
import sttp.apispec.openapi.circe.yaml.RichOpenAPI
import sttp.tapir.docs.openapi.OpenAPIDocsInterpreter
import sttp.tapir.server.http4s.Http4sServerInterpreter
import sttp.tapir.swagger.SwaggerUI

class OpenApiRoutes[M[_]: Async](endpoints: WalkerEndpoints) {

  private val e = List(
    endpoints.livenessEndpoint,
    endpoints.readinessEndpoint
  )

  private val openApiInfo: Info = Info(
    title = BuildInfo.name,
    version = BuildInfo.version
  )

  private val openApiYaml: String = OpenAPIDocsInterpreter().toOpenAPI(e, openApiInfo).toYaml

  val routes: HttpRoutes[M] = Http4sServerInterpreter[M]().toRoutes(SwaggerUI[M](openApiYaml))

}
