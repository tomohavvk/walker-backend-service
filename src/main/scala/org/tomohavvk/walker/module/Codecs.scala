package org.tomohavvk.walker.module

import org.tomohavvk.walker.http.endpoints.codecs.CommonCodecs
import org.tomohavvk.walker.http.endpoints.codecs.DeviceCodecs
import org.tomohavvk.walker.http.endpoints.codecs.ErrorCodecs
import org.tomohavvk.walker.http.endpoints.codecs.GroupCodecs
import org.tomohavvk.walker.http.endpoints.codecs.LocationCodecs
import org.tomohavvk.walker.http.endpoints.codecs.ProbeCodecs
import org.tomohavvk.walker.http.endpoints.schemas.EndpointSchemas
import org.tomohavvk.walker.serialization.json.ProtocolSerialization
import sttp.tapir.codec.enumeratum.TapirCodecEnumeratum
import sttp.tapir.generic.auto.SchemaDerivation
import sttp.tapir.generic.{Configuration => TapirConfiguration}
import sttp.tapir.json.circe.TapirJsonCirce

case class Codecs(
  probe:          ProbeCodecs,
  locationCodecs: LocationCodecs,
  deviceCodecs:   DeviceCodecs,
  groupCodecs:    GroupCodecs,
  commonCodecs:   CommonCodecs,
  errorCodecs:    ErrorCodecs)

object Codecs
    extends TapirJsonCirce
    with SchemaDerivation
    with ProtocolSerialization
    with TapirCodecEnumeratum
    with EndpointSchemas {

  implicit val customConfiguration: TapirConfiguration = TapirConfiguration.default.withSnakeCaseMemberNames

  val probeCodecs: ProbeCodecs         = ProbeCodecs()
  val locationCodecs: LocationCodecs   = LocationCodecs()
  val deviceCodecs: DeviceCodecs       = DeviceCodecs()
  val groupCodecs: GroupCodecs         = GroupCodecs()
  val commonCodecsCodecs: CommonCodecs = CommonCodecs()
  val errorCodecs: ErrorCodecs         = ErrorCodecs()

  def make: Codecs = Codecs(probeCodecs, locationCodecs, deviceCodecs, groupCodecs, commonCodecsCodecs, errorCodecs)

}
