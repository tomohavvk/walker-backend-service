package org.tomohavvk.walker.module

import cats.effect.kernel.Temporal
import io.odin.Logger
import org.tomohavvk.walker.module.ServiceModule.ServicesDeps
import org.tomohavvk.walker.persistence.Transactor
import org.tomohavvk.walker.streams.DeviceLocationEventStream

object StreamModule {

  case class StreamDeps[F[_], D[_]](deviceLocationEventStream: DeviceLocationEventStream[F, D])

  def make[F[_]: Temporal, D[_]](
    services:   ServicesDeps[F],
    resources:  ResourcesDeps[F],
    transactor: Transactor[F, D],
    loggerF:    Logger[F]
  ): StreamDeps[F, D] = {
    val deviceLocationEventStream =
      new DeviceLocationEventStream[F, D](services.locationService, resources.deviceLocationEventConsumer, loggerF)

    StreamDeps(deviceLocationEventStream)
  }
}
