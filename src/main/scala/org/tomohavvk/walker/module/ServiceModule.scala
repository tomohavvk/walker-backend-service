package org.tomohavvk.walker.module

import cats.Monad

import cats.mtl.Handle
import io.odin.Logger
import org.tomohavvk.walker.generation.TimeGen
import org.tomohavvk.walker.module.RepositoryModule.RepositoriesDeps
import org.tomohavvk.walker.persistence.Transactor
import org.tomohavvk.walker.protocol.errors.AppError
import org.tomohavvk.walker.services.DeviceService
import org.tomohavvk.walker.services.DeviceServiceImpl
import org.tomohavvk.walker.services.DevicesGroupService
import org.tomohavvk.walker.services.DeviceGroupServiceImpl
import org.tomohavvk.walker.services.GroupService
import org.tomohavvk.walker.services.GroupServiceImpl
import org.tomohavvk.walker.services.LocationService
import org.tomohavvk.walker.services.LocationServiceImpl

object ServiceModule {

  case class ServicesDeps[F[_]](
    locationService:     LocationService[F],
    deviceService:       DeviceService[F],
    groupService:        GroupService[F],
    devicesGroupService: DevicesGroupService[F])
  case class WalkerException(message: String) extends Throwable

  def make[F[_]: Monad, D[_]: Monad](
    repositoriesDeps: RepositoriesDeps[D],
    transactor:       Transactor[F, D],
    loggerF:          Logger[F]
  )(implicit HE:      Handle[F, AppError],
    HD:               Handle[D, AppError],
    T:                TimeGen[F]
  ): ServicesDeps[F] = {
    val locationService = new LocationServiceImpl[F, D](repositoriesDeps.deviceRepository,
                                                        repositoriesDeps.deviceLocationRepository,
                                                        transactor,
                                                        loggerF
    )
    val deviceService = new DeviceServiceImpl[F, D](repositoriesDeps.deviceRepository, transactor, loggerF)
    val groupService = new GroupServiceImpl[F, D](repositoriesDeps.groupRepository,
                                                  repositoriesDeps.deviceGroupRepository,
                                                  transactor,
                                                  loggerF
    )
    val deviceGroupService = new DeviceGroupServiceImpl[F, D](repositoriesDeps.groupRepository,
                                                              repositoriesDeps.deviceGroupRepository,
                                                              transactor,
                                                              loggerF
    )

    ServicesDeps(locationService, deviceService, groupService, deviceGroupService)
  }
}
