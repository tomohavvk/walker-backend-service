package org.tomohavvk.walker.module

import cats.effect.kernel.Sync
import io.odin.Logger
import org.tomohavvk.walker.module.ServiceModule.ServicesDeps
import org.tomohavvk.walker.persistence.Transactor
import org.tomohavvk.walker.streams.DeviceLocationEventStream

object StreamModule {

  case class StreamDeps[F[_], B[_]](deviceLocationEventStream: DeviceLocationEventStream[F, B])

  def make[F[_]: Sync, B[_]](
    services:   ServicesDeps[F],
    resources:  ResourcesDeps[F],
    transactor: Transactor[F, B],
    logger:     Logger[F]
  ): StreamDeps[F, B] = {
    val deviceLocationEventStream =
      new DeviceLocationEventStream[F, B](services.locationService, resources.deviceLocationEventConsumer, logger)

    StreamDeps(deviceLocationEventStream)
  }
}
