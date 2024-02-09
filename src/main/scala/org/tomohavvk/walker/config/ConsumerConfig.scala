package org.tomohavvk.walker.config

import org.tomohavvk.walker.config.ConsumerConfig.SchemaRegistryConfig

import scala.concurrent.duration.FiniteDuration

case class ConsumerConfig(
  topic:            String,
  bootstrapServers: String,
  groupId:          String,
  pollingTimeout:   FiniteDuration,
  requestTimeout:   FiniteDuration,
  closeTimeout:     FiniteDuration,
  schemaRegistry:   SchemaRegistryConfig)

object ConsumerConfig {

  case class SchemaRegistryConfig(endpoint: String)

}
