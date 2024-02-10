package org.tomohavvk.walker.module

import cats.effect.kernel.Sync
import org.tomohavvk.walker.persistence.repository.DeviceLocationRepository
import org.tomohavvk.walker.persistence.repository.DoobieDeviceLocationRepository
import org.tomohavvk.walker.protocol.errors.AppError
import org.tomohavvk.walker.utils.LiftConnectionIO

object RepositoryModule {

  case class RepositoriesDeps[F[_]](deviceLocationRepository: DeviceLocationRepository[F])

  def make[F[_]]()(implicit F: LiftConnectionIO[F, AppError]): RepositoriesDeps[F] =
    RepositoriesDeps(new DoobieDeviceLocationRepository[F]())
}
