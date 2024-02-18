package org.tomohavvk.walker.module

import cats.effect.kernel.Temporal
import org.tomohavvk.walker.module.ServiceModule.ServicesDeps
import org.tomohavvk.walker.streams.DeviceLocationEventStream

object StreamModule {

  case class StreamDeps[F[_], D[_]](deviceLocationEventStream: DeviceLocationEventStream[F, D])

  def make[F[_]: Temporal, D[_]](
    services:   ServicesDeps[F],
    resources:  ResourcesDeps[F]
  ): StreamDeps[F, D] = {
    val deviceLocationEventStream =
      new DeviceLocationEventStream[F, D](services.locationService, resources.deviceLocationEventConsumer)

    StreamDeps(deviceLocationEventStream)
  }
}
