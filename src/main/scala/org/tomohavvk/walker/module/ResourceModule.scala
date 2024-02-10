package org.tomohavvk.walker.module

import cats.effect.Async
import cats.effect.kernel.Resource
import com.zaxxer.hikari.HikariConfig
import doobie.hikari.HikariTransactor
import doobie.util.ExecutionContexts
import vulcan.Codec
import fs2.kafka._
import fs2.kafka.vulcan.AvroSettings
import fs2.kafka.vulcan.SchemaRegistryClientSettings
import fs2.kafka.vulcan.avroDeserializer
import org.tomohavvk.walker.EventConsumer
import org.tomohavvk.walker.config.AppConfig
import org.tomohavvk.walker.config.ConsumerConfig
import org.tomohavvk.walker.config.DatabaseConfig
import org.tomohavvk.walker.protocol.Types.Key
import org.tomohavvk.walker.protocol.events.Event
import org.tomohavvk.walker.serialization.avro.EventCodecs

import java.nio.charset.StandardCharsets

case class ResourcesDeps[F[_]](
  transactor:                  HikariTransactor[F],
  deviceLocationEventConsumer: EventConsumer[F, Key, Event])

object ResourceModule extends EventCodecs {

  def make[F[_]: Async](configs: Configs): Resource[F, ResourcesDeps[F]] =
    for {
      transactor <- makeTransactor(configs.database)
      consumer   <- makeConsumerResource[F, Event](configs.deviceLocationEventConsumer)
    } yield ResourcesDeps[F](transactor, EventConsumer(configs.deviceLocationEventConsumer.topic, consumer))

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
        .withPollTimeout(consumerConfig.pollingTimeout)
        .withRequestTimeout(consumerConfig.requestTimeout)
        .withCloseTimeout(consumerConfig.closeTimeout)
        .withAutoOffsetReset(AutoOffsetReset.Latest)

    KafkaConsumer.resource(consumerSettings)
  }

  private def hikariConfig: DatabaseConfig => HikariConfig =
    config => {
      import config._

      val conf = new HikariConfig()
      conf.setJdbcUrl(url)
      conf.setUsername(user)
      conf.setPassword(password)
      conf.setConnectionTimeout(connectionTimeout.toMillis)
      conf.setValidationTimeout(validationTimeout.toMillis)
      conf.setMaximumPoolSize(maximumPoolSize)
      conf.setDriverClassName(driver)
      conf.setConnectionInitSql("SET TIME ZONE 'UTC'")
      conf.setConnectionTestQuery("select 1")
      conf
    }

  private def makeTransactor[F[_]: Async](config: DatabaseConfig): Resource[F, HikariTransactor[F]] =
    for {
      connectionPool   <- ExecutionContexts.fixedThreadPool(config.maximumPoolSize)
      hikariTransactor <- HikariTransactor.fromHikariConfigCustomEc[F](hikariConfig(config), connectionPool)
    } yield hikariTransactor

}
