package org.tomohavvk.walker.module

import cats.effect.kernel.{Clock, Spawn, Sync}
import io.odin.Logger
import org.tomohavvk.walker.module.RepositoryModule.RepositoriesDeps
import org.tomohavvk.walker.persistence.Transactor
import org.tomohavvk.walker.services.LocationService
import org.tomohavvk.walker.services.LocationServiceImpl
import org.tomohavvk.walker.utils.ContextFlow

object ServiceModule {

  case class ServicesDeps[F[_]](locationService: LocationService[F])

  def make[F[_]: Sync, B[_]](
    repositoriesDeps: RepositoriesDeps[B],
    transactor:       Transactor[F, B],
    logger:           Logger[ContextFlow[F, *]]
  ): ServicesDeps[F] = {
    val locationService = new LocationServiceImpl[F, B](repositoriesDeps.deviceLocationRepository, transactor, logger)

    ServicesDeps(locationService)
  }
}
