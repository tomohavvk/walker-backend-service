import sbt._

object Library {

  object versions {
    val blaze         = "0.23.16"
    val catsCore      = "2.9.0"
    val catsEffect    = "3.4.11"
    val chimney       = "0.7.5"
    val circe         = "0.14.5"
    val circeGeneric  = "0.14.3"
    val doobie        = "1.0.0-RC5"
    val enumeratum    = "1.7.2"
    val flyway        = "9.16.0"
    val fs2Kafka      = "3.2.0"
    val kindProjector = "0.13.2"
    val newtype       = "0.4.4"
    val odin          = "0.13.0"
    val pureConfig    = "0.17.5"
    val tapir         = "1.9.9"
  }

  object orgs {
    val circe      = "io.circe"
    val enumeratum = "com.beachape"
    val estatico   = "io.estatico"
    val flyway     = "org.flywaydb"
    val http4s     = "org.http4s"
    val odin       = "com.github.valskalla"
    val pureConfig = "com.github.pureconfig"
    val scalaland  = "io.scalaland"
    val sttp       = "com.softwaremill.sttp"
    val tpolecat   = "org.tpolecat"
    val typeLevel  = "org.typelevel"
    val fs2        = "com.github.fd4s"
  }

  object Cats {
    val catsCore   = orgs.typeLevel %% "cats-core"   % versions.catsCore
    val catsEffect = orgs.typeLevel %% "cats-effect" % versions.catsEffect
  }

  object Doobie {
    val core     = orgs.tpolecat %% "doobie-core"     % versions.doobie
    val hikari   = orgs.tpolecat %% "doobie-hikari"   % versions.doobie
    val postgres = orgs.tpolecat %% "doobie-postgres" % versions.doobie
  }

  object Db {
    val flyway = orgs.flyway % "flyway-core" % versions.flyway
  }

  object Http {
    val core              = s"${orgs.sttp}.tapir"   %% "tapir-core"          % versions.tapir
    val enumeratum        = s"${orgs.sttp}.tapir"   %% "tapir-enumeratum"    % versions.tapir
    val http4sServer      = s"${orgs.sttp}.tapir"   %% "tapir-http4s-server" % versions.tapir
    val jsonCirce         = s"${orgs.sttp}.tapir"   %% "tapir-json-circe"    % versions.tapir
    val openapiDocs       = s"${orgs.sttp}.tapir"   %% "tapir-openapi-docs"  % versions.tapir
    val swaggerUI         = s"${orgs.sttp}.tapir"   %% "tapir-swagger-ui"    % versions.tapir
    val http4sBlazeServer = orgs.http4s             %% "http4s-blaze-server" % versions.blaze
    val http4sDsl         = orgs.http4s             %% "http4s-dsl"          % versions.blaze
    val openApiCirce      = s"${orgs.sttp}.apispec" %% "openapi-circe-yaml"  % "0.7.4"
  }

  object Logging {
    val odin = orgs.odin %% "odin-core" % versions.odin
  }

  object Circe {
    val core          = orgs.circe %% "circe-core"           % versions.circe
    val parser        = orgs.circe %% "circe-parser"         % versions.circe
    val genericExtras = orgs.circe %% "circe-generic-extras" % versions.circeGeneric
  }

  object Config {
    val pureconfigCats       = orgs.pureConfig %% "pureconfig-cats-effect" % versions.pureConfig
    val pureconfigEnumeratum = orgs.pureConfig %% "pureconfig-enumeratum"  % versions.pureConfig
    val pureconfigGeneric    = orgs.pureConfig %% "pureconfig-generic"     % versions.pureConfig
  }

  object Utils {
    val chimney         = orgs.scalaland        %% "chimney"          % versions.chimney
    val newType         = orgs.estatico         %% "newtype"          % versions.newtype
    val enumeratum      = orgs.enumeratum       %% "enumeratum"       % versions.enumeratum
    val enumeratumCirce = orgs.enumeratum       %% "enumeratum-circe" % versions.enumeratum
    val nanoid          = "com.aventrix.jnanoid" % "jnanoid"          % "1.0.1"
  }

  object CompilerPlugins {
    val macros        = "org.scalamacros" % "paradise"       % "2.1.1" cross CrossVersion.full
    val kindProjector = orgs.typeLevel    % "kind-projector" % versions.kindProjector cross CrossVersion.full
  }

  object Kafka {
    val fs2Kafka       = orgs.fs2 %% "fs2-kafka"        % versions.fs2Kafka
    val fs2KafkaVulcan = orgs.fs2 %% "fs2-kafka-vulcan" % versions.fs2Kafka
  }

  object Tests {
    val catsScalaTest       = orgs.typeLevel               %% "cats-effect-testing-scalatest" % "1.4.0"
    val munitCatsEffect     = orgs.typeLevel               %% "munit-cats-effect-3"           % "1.0.7"
    val scalaMock           = "org.scalamock"              %% "scalamock-scalatest-support"   % "3.6.0"
    val scalaTest           = "org.scalatest"              %% "scalatest"                     % "3.2.15"
    val scalaCheckShapeless = "com.github.alexarchambault" %% "scalacheck-shapeless_1.15"     % "1.3.0"
    val scalatestplus       = "org.scalatestplus"          %% "scalacheck-1-15"               % "3.2.11.0"
  }

  val testLibs = Seq(
    Tests.scalaMock           % "test",
    Tests.scalaTest           % "test",
    Tests.scalatestplus       % "test",
    Tests.munitCatsEffect     % "test",
    Tests.catsScalaTest       % "test",
    Tests.scalaCheckShapeless % "test"
  )

  val appLibs = Seq(
//  "org.slf4j" % "slf4j-api" % "2.0.0-alpha5",
//  "ch.qos.logback" % "logback-classic" % "1.4.8",
    Cats.catsCore,
    Circe.genericExtras,
    Circe.parser,
    Config.pureconfigCats,
    Config.pureconfigEnumeratum,
    Config.pureconfigGeneric,
    Db.flyway,
    Doobie.core,
    Doobie.hikari,
    Doobie.postgres,
    Http.core,
    Http.enumeratum,
    Http.http4sBlazeServer,
    Http.http4sDsl,
    Http.http4sServer,
    Http.jsonCirce,
    Http.openapiDocs,
    Http.openApiCirce,
    Http.swaggerUI,
    Kafka.fs2Kafka,
    Kafka.fs2KafkaVulcan,
    Logging.odin,
    Utils.chimney,
    Utils.enumeratum,
    Utils.enumeratumCirce,
    Utils.nanoid,
    Utils.newType
  ) ++ testLibs
}
