package org.tomohavvk.walker.module

import org.tomohavvk.walker.persistence.repository.DeviceLocationRepository
import org.tomohavvk.walker.persistence.repository.DeviceRepository
import org.tomohavvk.walker.persistence.repository.DoobieDeviceLocationRepository
import org.tomohavvk.walker.persistence.repository.DoobieDeviceRepository
import org.tomohavvk.walker.protocol.errors.AppError
import org.tomohavvk.walker.utils.LiftConnectionIO

object RepositoryModule {

  case class RepositoriesDeps[D[_]](
    deviceRepository:         DeviceRepository[D],
    deviceLocationRepository: DeviceLocationRepository[D])

  def make[D[_]]()(implicit D: LiftConnectionIO[D, AppError]): RepositoriesDeps[D] =
    RepositoriesDeps(new DoobieDeviceRepository[D](), new DoobieDeviceLocationRepository[D]())
}
