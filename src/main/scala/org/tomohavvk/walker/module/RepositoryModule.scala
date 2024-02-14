package org.tomohavvk.walker.module

import cats.Monad
import org.tomohavvk.walker.persistence.repository.DeviceGroupRepository
import org.tomohavvk.walker.persistence.repository.DeviceLocationRepository
import org.tomohavvk.walker.persistence.repository.DeviceRepository
import org.tomohavvk.walker.persistence.repository.DoobieDeviceGroupRepository
import org.tomohavvk.walker.persistence.repository.DoobieDeviceLocationRepository
import org.tomohavvk.walker.persistence.repository.DoobieDeviceRepository
import org.tomohavvk.walker.persistence.repository.DoobieGroupRepository
import org.tomohavvk.walker.persistence.repository.GroupRepository
import org.tomohavvk.walker.protocol.errors.AppError
import org.tomohavvk.walker.utils.LiftConnectionIO

object RepositoryModule {

  case class RepositoriesDeps[D[_]](
    deviceRepository:         DeviceRepository[D],
    groupRepository:          GroupRepository[D],
    deviceGroupRepository:    DeviceGroupRepository[D],
    deviceLocationRepository: DeviceLocationRepository[D])

  def make[D[_]: Monad]()(implicit D: LiftConnectionIO[D, AppError]): RepositoriesDeps[D] =
    RepositoriesDeps(new DoobieDeviceRepository[D](),
                     new DoobieGroupRepository[D](),
                     new DoobieDeviceGroupRepository[D](),
                     new DoobieDeviceLocationRepository[D]()
    )
}
