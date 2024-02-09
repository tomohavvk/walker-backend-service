package org.tomohavvk.walker.module

import cats.effect.kernel.Sync
import io.odin.Logger
import org.tomohavvk.walker.services.LocationService
import org.tomohavvk.walker.services.LocationServiceImpl
import org.tomohavvk.walker.utils.ContextFlow

object ServiceModule {

  case class ServicesDeps[F[_]](locationService: LocationService[F])

  def make[F[_]: Sync](logger: Logger[ContextFlow[F, *]]): ServicesDeps[F] = {
    val locationService = new LocationServiceImpl[F](logger)

    ServicesDeps(locationService)
  }
}
