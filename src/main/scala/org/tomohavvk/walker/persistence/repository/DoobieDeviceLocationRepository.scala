package org.tomohavvk.walker.persistence.repository

import org.tomohavvk.walker.persistence._
import org.tomohavvk.walker.protocol.Types.DeviceId
import org.tomohavvk.walker.protocol.entities.DeviceLocationEntity
import org.tomohavvk.walker.protocol.errors.AppError
import org.tomohavvk.walker.utils.LiftConnectionIO

class DoobieDeviceLocationRepository[F[_]](implicit F: LiftConnectionIO[F, AppError])
    extends DeviceLocationRepository[F]
    with DeviceLocationStatements {

  override def upsertBatch(entities: List[DeviceLocationEntity]): F[Int] =
    F.lift(upsertQuery(entities))

  override def findLastById(deviceId: DeviceId): F[Option[DeviceLocationEntity]] =
    F.lift(findLastByIdSQuery(deviceId).option)
}
