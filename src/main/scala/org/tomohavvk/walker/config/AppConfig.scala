package org.tomohavvk.walker.config

import enumeratum.Enum
import enumeratum.EnumEntry
import enumeratum.EnumEntry.Snakecase

import scala.collection.immutable

case class AppConfig(env: AppConfig.Env)

object AppConfig {

  sealed trait Env extends EnumEntry with Snakecase

  object Env extends Enum[Env] {

    case object Dev extends Env

    case object Prod extends Env

    override val values: immutable.IndexedSeq[Env] = findValues
  }

}
