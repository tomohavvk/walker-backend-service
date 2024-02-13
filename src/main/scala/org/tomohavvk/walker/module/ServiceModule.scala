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

object ServiceModule {

  case class ServicesDeps[F[_]](locationService: LocationService[F], deviceService: DeviceService[F])

  def make[F[_]: Sync, D[_]: Sync](
    repositoriesDeps: RepositoriesDeps[D],
    transactor:       Transactor[F, D],
    loggerF:          Logger[F],
    loggerD:          Logger[D]
  )(implicit HF:      Handle[F, AppError],
    HD:               Handle[D, AppError]
  ): ServicesDeps[F] = {
    val locationService = new LocationServiceImpl[F, D](repositoriesDeps.deviceRepository,
                                                        repositoriesDeps.deviceLocationRepository,
                                                        transactor,
                                                        loggerF,
                                                        loggerD
    )
    val deviceService = new DeviceServiceImpl[F, D](repositoriesDeps.deviceRepository, transactor, loggerF)

    ServicesDeps(locationService, deviceService)
  }
}
