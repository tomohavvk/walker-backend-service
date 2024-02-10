package org.tomohavvk.walker.config

import scala.concurrent.duration.FiniteDuration

case class DatabaseConfig(
  maximumPoolSize:   Int,
  driver:            String,
  url:               String,
  user:              String,
  password:          String,
  connectionTimeout: FiniteDuration,
  validationTimeout: FiniteDuration)
