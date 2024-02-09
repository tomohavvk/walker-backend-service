package org.tomohavvk.walker.serialization.avro

import cats.Monoid
import org.scalacheck.ScalacheckShapeless
import org.scalatest.EitherValues
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import org.tomohavvk.walker.protocol.events.Event
import vulcan.Codec

import scala.io.Source

class EventCodecsSpec
    extends AnyWordSpec
    with Matchers
    with EventCodecs
    with EitherValues
    with ScalacheckShapeless
    with ArbitrarySpec
    with ScalaCheckPropertyChecks {

  "AppEventsCodecs" should {
    "serialize and deserialize Event" in {
      forAll { event: Event =>
        val bytes       = Codec.toBinary(event).value
        val resultEvent = Codec.fromBinary[Event](bytes, eventAvroCodec.schema.value).value
        resultEvent shouldBe event
      }
    }

    "Event codec working as expected" in {
      val expected = getResource("AppEvent-avro-schema.json")
      val actual   = eventAvroCodec.schema.value.toString.stripMargin

      actual shouldBe expected
    }

    "DeviceLocationEvent codec working as expected" in {
      val expected = getResource("DeviceLocationEvent-avro-schema.json")
      val actual   = codecDeviceLocationEvent.schema.value.toString.stripMargin

      actual shouldBe expected
    }
  }

  private def getResource(resource: String): String = {
    val source = Source.fromResource(resource)
    val schema =
      source.mkString.replaceAll("\n", Monoid[String].empty).replaceAll(" ", Monoid[String].empty).stripMargin
    source.close()
    schema
  }

}
