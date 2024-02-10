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
import org.tomohavvk.walker.protocol.Types.Accuracy
import org.tomohavvk.walker.protocol.Types.Altitude
import org.tomohavvk.walker.protocol.Types.AltitudeAccuracy
import org.tomohavvk.walker.protocol.Types.Bearing
import org.tomohavvk.walker.protocol.Types.DeviceId
import org.tomohavvk.walker.protocol.Types.Latitude
import org.tomohavvk.walker.protocol.Types.Longitude
import org.tomohavvk.walker.protocol.Types.Speed
import org.tomohavvk.walker.protocol.Types.UnixTime
import org.tomohavvk.walker.protocol.errors.AppError
import org.tomohavvk.walker.protocol.errors.InternalError
import org.tomohavvk.walker.protocol.views.AcknowledgeView
import org.tomohavvk.walker.protocol.views.DeviceLocationView
import org.tomohavvk.walker.protocol.views.ProbeView

trait ProtocolSerialization extends CirceConfig {

  implicit def stringEnumCodec[T <: StringEnumEntry](implicit enum: StringEnum[T]): Codec[T] =
    Codec.from(Circe.decoder[String, T](enum), Circe.encoder[String, T](enum))

  implicit val codecDeviceId: Codec[DeviceId]   = Codec.from(DeviceId.deriving, DeviceId.deriving)
  implicit val codecLatitude: Codec[Latitude]   = Codec.from(Latitude.deriving, Latitude.deriving)
  implicit val codecLongitude: Codec[Longitude] = Codec.from(Longitude.deriving, Longitude.deriving)
  implicit val codecAccuracy: Codec[Accuracy]   = Codec.from(Accuracy.deriving, Accuracy.deriving)
  implicit val codecAltitude: Codec[Altitude]   = Codec.from(Altitude.deriving, Altitude.deriving)
  implicit val codecSpeed: Codec[Speed]         = Codec.from(Speed.deriving, Speed.deriving)
  implicit val codecBearing: Codec[Bearing]     = Codec.from(Bearing.deriving, Bearing.deriving)
  implicit val codecUnixTime: Codec[UnixTime]   = Codec.from(UnixTime.deriving, UnixTime.deriving)

  implicit val codecAltitudeAccuracy: Codec[AltitudeAccuracy] =
    Codec.from(AltitudeAccuracy.deriving, AltitudeAccuracy.deriving)

  implicit val codecDeviceLocationView: Codec[DeviceLocationView] = deriveConfiguredCodec[DeviceLocationView]
  implicit val codecAcknowledgeView: Codec[AcknowledgeView]       = deriveConfiguredCodec[AcknowledgeView]
  implicit val codecProbesView: Codec[ProbeView]                  = deriveConfiguredCodec[ProbeView]

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

}
object ProtocolSerialization extends ProtocolSerialization
