package org.tomohavvk.walker.persistence

import doobie.postgres.Instances
import doobie.postgres.JavaTimeInstances
import doobie.util.meta.Meta
import io.circe.Json
import io.circe.parser.parse
import org.postgresql.util.PGobject
import org.tomohavvk.walker.protocol.Types._
import org.tomohavvk.walker.serialization.json.ProtocolSerialization

trait DoobieMeta extends Instances with JavaTimeInstances with ProtocolSerialization {

  implicit lazy val metaDeviceId: Meta[DeviceId]                 = DeviceId.deriving
  implicit lazy val metaGroupId: Meta[GroupId]                   = GroupId.deriving
  implicit lazy val metaDeviceName: Meta[DeviceName]             = DeviceName.deriving
  implicit lazy val metaDeviceCount: Meta[DeviceCount]           = DeviceCount.deriving
  implicit lazy val metaGroupName: Meta[GroupName]               = GroupName.deriving
  implicit lazy val metaIsPrivate: Meta[IsPrivate]               = IsPrivate.deriving
  implicit lazy val metaCreatedAt: Meta[CreatedAt]               = CreatedAt.deriving
  implicit lazy val metaUpdatedAt: Meta[UpdatedAt]               = UpdatedAt.deriving
  implicit lazy val metaLatitude: Meta[Latitude]                 = Latitude.deriving
  implicit lazy val metaLongitude: Meta[Longitude]               = Longitude.deriving
  implicit lazy val metaAccuracy: Meta[Accuracy]                 = Accuracy.deriving
  implicit lazy val metaAltitude: Meta[Altitude]                 = Altitude.deriving
  implicit lazy val metaSpeed: Meta[Speed]                       = Speed.deriving
  implicit lazy val metaUnixTime: Meta[UnixTime]                 = UnixTime.deriving
  implicit lazy val metaBearing: Meta[Bearing]                   = Bearing.deriving
  implicit lazy val metaAltitudeAccuracy: Meta[AltitudeAccuracy] = AltitudeAccuracy.deriving
  implicit lazy val metaSearch: Meta[Search]                     = Search.deriving
  implicit lazy val metaLimit: Meta[Limit]                       = Limit.deriving
  implicit lazy val metaOffset: Meta[Offset]                     = Offset.deriving

  implicit lazy val jsonMeta: Meta[Json] =
    Meta.Advanced
      .other[PGobject]("json")
      .timap[Json](a => parse(a.getValue).right.get) { a =>
        val o = new PGobject
        o.setType("json")
        o.setValue(a.noSpaces)
        o
      }
}
