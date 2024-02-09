package org.tomohavvk.walker.serialization.json

import io.circe.generic.extras.Configuration

trait CirceConfig {
  implicit val circeConfiguration: Configuration = Configuration.default.withSnakeCaseMemberNames
}
