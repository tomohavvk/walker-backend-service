package org.tomohavvk.walker.serialization.json

import enumeratum.values.Circe
import enumeratum.values.StringEnum
import enumeratum.values.StringEnumEntry
import io.circe.generic.extras.semiauto.deriveConfiguredCodec
import io.circe.syntax.EncoderOps
import io.circe.Codec
import io.circe.Decoder
import io.circe.Encoder
import io.circe.Json
import org.tomohavvk.walker.protocol.DeviceLocation
import org.tomohavvk.walker.protocol.Types._
import org.tomohavvk.walker.protocol.errors.AppError
import org.tomohavvk.walker.protocol.errors.InternalError
import org.tomohavvk.walker.protocol.commands.RegisterDeviceCommand
import org.tomohavvk.walker.protocol.commands.CreateGroupCommand
import org.tomohavvk.walker.protocol.views.AcknowledgeView
import org.tomohavvk.walker.protocol.views.DeviceGroupView
import org.tomohavvk.walker.protocol.views.DeviceLocationView
import org.tomohavvk.walker.protocol.views.DeviceView
import org.tomohavvk.walker.protocol.views.GroupView
import org.tomohavvk.walker.protocol.views.ProbeView
import org.tomohavvk.walker.protocol.ws.GroupJoin
import org.tomohavvk.walker.protocol.ws.GroupJoined
import org.tomohavvk.walker.protocol.ws.GroupsSearch
import org.tomohavvk.walker.protocol.ws.GroupsSearched
import org.tomohavvk.walker.protocol.ws.LocationPersist
import org.tomohavvk.walker.protocol.ws.LocationPersisted
import org.tomohavvk.walker.protocol.ws.MessageInType
import org.tomohavvk.walker.protocol.ws.MessageOutType
import org.tomohavvk.walker.protocol.ws.WSMessageIn
import org.tomohavvk.walker.protocol.ws.WSMessageOut

trait ProtocolSerialization extends CirceConfig {

  implicit def stringEnumCodec[T <: StringEnumEntry](implicit enum: StringEnum[T]): Codec[T] =
    Codec.from(Circe.decoder[String, T](enum), Circe.encoder[String, T](enum))

  implicit val codecDeviceId: Codec[DeviceId]       = Codec.from(DeviceId.deriving, DeviceId.deriving)
  implicit val codecGroupId: Codec[GroupId]         = Codec.from(GroupId.deriving, GroupId.deriving)
  implicit val codecIsPrivate: Codec[IsPrivate]     = Codec.from(IsPrivate.deriving, IsPrivate.deriving)
  implicit val codecDeviceName: Codec[DeviceName]   = Codec.from(DeviceName.deriving, DeviceName.deriving)
  implicit val codecDeviceCount: Codec[DeviceCount] = Codec.from(DeviceCount.deriving, DeviceCount.deriving)
  implicit val codecGroupName: Codec[GroupName]     = Codec.from(GroupName.deriving, GroupName.deriving)
  implicit val codecLatitude: Codec[Latitude]       = Codec.from(Latitude.deriving, Latitude.deriving)
  implicit val codecLongitude: Codec[Longitude]     = Codec.from(Longitude.deriving, Longitude.deriving)
  implicit val codecAccuracy: Codec[Accuracy]       = Codec.from(Accuracy.deriving, Accuracy.deriving)
  implicit val codecAltitude: Codec[Altitude]       = Codec.from(Altitude.deriving, Altitude.deriving)
  implicit val codecSpeed: Codec[Speed]             = Codec.from(Speed.deriving, Speed.deriving)
  implicit val codecBearing: Codec[Bearing]         = Codec.from(Bearing.deriving, Bearing.deriving)
  implicit val codecUnixTime: Codec[UnixTime]       = Codec.from(UnixTime.deriving, UnixTime.deriving)
  implicit val codecCreatedAt: Codec[CreatedAt]     = Codec.from(CreatedAt.deriving, CreatedAt.deriving)
  implicit val codecUpdatedAt: Codec[UpdatedAt]     = Codec.from(UpdatedAt.deriving, UpdatedAt.deriving)
  implicit val codecSearch: Codec[Search]           = Codec.from(Search.deriving, Search.deriving)
  implicit val codecLimit: Codec[Limit]             = Codec.from(Limit.deriving, Limit.deriving)
  implicit val codecOffset: Codec[Offset]           = Codec.from(Offset.deriving, Offset.deriving)

  implicit val codecAltitudeAccuracy: Codec[AltitudeAccuracy] =
    Codec.from(AltitudeAccuracy.deriving, AltitudeAccuracy.deriving)

  implicit val codecDeviceLocationView: Codec[DeviceLocationView]     = deriveConfiguredCodec[DeviceLocationView]
  implicit val codecAcknowledgeView: Codec[AcknowledgeView]           = deriveConfiguredCodec[AcknowledgeView]
  implicit val codecDeviceView: Codec[DeviceView]                     = deriveConfiguredCodec[DeviceView]
  implicit val codecDeviceGroupView: Codec[DeviceGroupView]           = deriveConfiguredCodec[DeviceGroupView]
  implicit val codecGroupView: Codec[GroupView]                       = deriveConfiguredCodec[GroupView]
  implicit val codecCreateDeviceCommand: Codec[RegisterDeviceCommand] = deriveConfiguredCodec[RegisterDeviceCommand]
  implicit val codecCreateGroupCommand: Codec[CreateGroupCommand]     = deriveConfiguredCodec[CreateGroupCommand]
  implicit val codecProbesView: Codec[ProbeView]                      = deriveConfiguredCodec[ProbeView]

  implicit lazy val codecAppError: Codec[AppError] = Codec.from(appErrorDecoder, appErrorEncoder)

  private lazy val appErrorEncoder: Encoder[AppError] =
    er =>
      Json.obj(
        ("message", er.apiMessage.value.asJson),
        ("id", er.code.value.asJson)
      )

  private lazy val appErrorDecoder: Decoder[AppError] =
    c =>
      for {
        msg  <- c.downField("message").as[String]
        hint <- c.downField("hint").as[Option[String]]
      } yield InternalError(s"Error occur with msg: $msg and hint: $hint")

  // -- WS
  implicit val codecDeviceLocation: Codec[DeviceLocation] =
    deriveConfiguredCodec[DeviceLocation]

  implicit val codecLocationPersist: Codec[LocationPersist] =
    deriveConfiguredCodec[LocationPersist]
  implicit val codecGroupJoin: Codec[GroupJoin]       = deriveConfiguredCodec[GroupJoin]
  implicit val codecGroupsSearch: Codec[GroupsSearch] = deriveConfiguredCodec[GroupsSearch]

  implicit val encoderWSMessageIn: Encoder[WSMessageIn] = Encoder.instance[WSMessageIn] { message =>
    val baseJson = message match {
      case message: LocationPersist => codecLocationPersist(message)
      case message: GroupJoin       => codecGroupJoin(message)
      case message: GroupsSearch    => codecGroupsSearch(message)
    }

    baseJson.mapObject(_.add("type", message.`type`.asJson))
  }

  implicit val decoderWSMessageIn: Decoder[WSMessageIn] = cursor =>
    cursor.downField("type").as[MessageInType].flatMap {
      case MessageInType.LocationPersist => codecLocationPersist(cursor)
      case MessageInType.GroupJoin       => codecGroupJoin(cursor)
      case MessageInType.GroupsSearch    => codecGroupsSearch(cursor)
    }

  implicit val codecLocationPersisted: Codec[LocationPersisted] = deriveConfiguredCodec[LocationPersisted]
  implicit val codecGroupJoined: Codec[GroupJoined]             = deriveConfiguredCodec[GroupJoined]
  implicit val codecGroupsSearched: Codec[GroupsSearched]       = deriveConfiguredCodec[GroupsSearched]

  implicit val encoderWSMessageOut: Encoder[WSMessageOut] = Encoder.instance[WSMessageOut] { message =>
    val baseJson = message match {
      case message: LocationPersisted => codecLocationPersisted(message)
      case message: GroupJoined       => codecGroupJoined(message)
      case message: GroupsSearched    => codecGroupsSearched(message)
    }

    baseJson.mapObject(_.add("type", message.`type`.asJson))
  }

  implicit val decoderWSMessageOut: Decoder[WSMessageOut] = cursor =>
    cursor.downField("type").as[MessageOutType].flatMap {
      case MessageOutType.LocationPersisted => codecLocationPersisted(cursor)
      case MessageOutType.GroupJoined       => codecGroupJoined(cursor)
      case MessageOutType.GroupsSearched    => codecGroupsSearched(cursor)
    }
}

object ProtocolSerialization extends ProtocolSerialization
