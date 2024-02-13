package org.tomohavvk.walker.module

import cats.effect.Async
import cats.effect.kernel.Resource
import vulcan.Codec
import fs2.kafka._
import fs2.kafka.vulcan.AvroSettings
import fs2.kafka.vulcan.SchemaRegistryClientSettings
import fs2.kafka.vulcan.avroDeserializer
import org.tomohavvk.walker.EventConsumer
import org.tomohavvk.walker.config.ConsumerConfig
import org.tomohavvk.walker.protocol.Types.Key
import org.tomohavvk.walker.protocol.events.Event
import org.tomohavvk.walker.serialization.avro.EventCodecs

import java.nio.charset.StandardCharsets

case class ResourcesDeps[F[_]](
  deviceLocationEventConsumer: EventConsumer[F, Key, Event])

object ResourceModule extends EventCodecs {

  def make[F[_]: Async](configs: Configs): Resource[F, ResourcesDeps[F]] =
    for {
      consumer <- makeConsumerResource[F, Event](configs.deviceLocationEventConsumer)
    } yield ResourcesDeps[F](EventConsumer(configs.deviceLocationEventConsumer.topic, consumer))

  private def makeConsumerResource[F[_]: Async, V: Codec](
    consumerConfig: ConsumerConfig
  ): Resource[F, KafkaConsumer[F, Key, V]] = {
    implicit val keyDeserializer: Deserializer[F, Key] = Deserializer.uuid[F](StandardCharsets.UTF_8)

    implicit val valueDeserializer: Resource[F, ValueDeserializer[F, V]] =
      avroDeserializer[V].forValue(
        AvroSettings(SchemaRegistryClientSettings[F](consumerConfig.schemaRegistry.endpoint))
      )

    val consumerSettings =
      ConsumerSettings[F, Key, V]
        .withBootstrapServers(consumerConfig.bootstrapServers)
        .withGroupId(consumerConfig.groupId)
        .withMaxPollInterval(consumerConfig.pollingTimeout)
//        .withMaxPollRecords(100)
//        .withMaxPrefetchBatches(100)
        .withRequestTimeout(consumerConfig.requestTimeout)
        .withCloseTimeout(consumerConfig.closeTimeout)
        .withAutoOffsetReset(AutoOffsetReset.Earliest)

    KafkaConsumer.resource(consumerSettings)
  }
}
