package org.tomohavvk.walker.module

import cats.effect.kernel.Sync
import cats.mtl.Handle
import io.odin.Logger
import org.tomohavvk.walker.module.RepositoryModule.RepositoriesDeps
import org.tomohavvk.walker.persistence.Transactor
import org.tomohavvk.walker.protocol.errors.AppError
import org.tomohavvk.walker.services.DeviceService
import org.tomohavvk.walker.services.DeviceServiceImpl
import org.tomohavvk.walker.services.LocationService
import org.tomohavvk.walker.services.LocationServiceImpl
import org.tomohavvk.walker.utils.ContextFlow

object ServiceModule {

  case class ServicesDeps[F[_]](locationService: LocationService[F], deviceService: DeviceService[F])

  def make[F[_]: Sync, B[_]: Sync](
    repositoriesDeps: RepositoriesDeps[B],
    transactor:       Transactor[F, B],
    loggerF:          Logger[ContextFlow[F, *]],
    loggerB:          Logger[ContextFlow[B, *]]
  )(implicit HF:      Handle[F, AppError],
    HB:               Handle[B, Throwable]
  ): ServicesDeps[F] = {
    val locationService = new LocationServiceImpl[F, B](repositoriesDeps.deviceRepository,
                                                        repositoriesDeps.deviceLocationRepository,
                                                        transactor,
                                                        loggerF,
                                                        loggerB
    )
    val deviceService = new DeviceServiceImpl[F, B](repositoriesDeps.deviceRepository, transactor)

    ServicesDeps(locationService, deviceService)
  }
}
