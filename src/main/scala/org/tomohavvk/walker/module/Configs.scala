package org.tomohavvk.walker.module

import cats.effect.kernel.Sync
import cats.implicits.toFunctorOps
import com.typesafe.config.ConfigFactory
import org.tomohavvk.walker.config.AppConfig
import org.tomohavvk.walker.config.ConsumerConfig
import org.tomohavvk.walker.config.DatabaseConfig
import org.tomohavvk.walker.config.ServerConfig
import pureconfig.ConfigSource
import pureconfig.generic.auto._
import pureconfig.module.enumeratum._
import pureconfig.module.catseffect.syntax.CatsEffectConfigSource

case class Configs(
  app:                         AppConfig,
  server:                      ServerConfig,
  database:                    DatabaseConfig,
  deviceLocationEventConsumer: ConsumerConfig)

object Configs {

  def make[F[_]](implicit F: Sync[F]): F[Right[Nothing, Configs]] =
    ConfigSource
      .fromConfig(ConfigFactory.load())
      .loadF[F, Configs]
      .map(Right(_))
}
