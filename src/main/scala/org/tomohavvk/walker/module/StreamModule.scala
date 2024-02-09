package org.tomohavvk.walker.module

import cats.effect.kernel.Sync
import io.odin.Logger
import org.tomohavvk.walker.streams.DeviceLocationEventStream

object StreamModule {

  case class StreamDeps[F[_]](deviceLocationEventStream: DeviceLocationEventStream[F])

  def make[F[_]: Sync](resources: ResourcesDeps[F], logger: Logger[F]): StreamDeps[F] = {
    val deviceLocationEventStream = new DeviceLocationEventStream[F](resources.consumer, logger)

    StreamDeps(deviceLocationEventStream)
  }
}
