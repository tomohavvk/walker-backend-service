package org.tomohavvk.walker.persistence

import cats.Apply
import cats.effect.Sync
import cats.implicits._
import io.odin.Logger
import org.flywaydb.core.Flyway
import org.flywaydb.core.api.output.CleanResult
import org.flywaydb.core.api.output.MigrateOutput
import org.flywaydb.core.api.output.MigrateResult
import org.tomohavvk.walker.config.DatabaseConfig

import scala.jdk.CollectionConverters.collectionAsScalaIterableConverter

object FlywayMigration {

  def migrate(url: String, username: String, password: String): MigrateResult =
    loadFlyway(url, username, password)
      .migrate()

  def cleanAndMigrate(url: String, username: String, password: String): (CleanResult, MigrateResult) = {
    val flyway = loadFlyway(url, username, password)

    flyway.clean() -> flyway.migrate()
  }

  private def loadFlyway(url: String, username: String, password: String): Flyway =
    Flyway
      .configure()
      .loggers()
      .dataSource(url, username, password)
      .load()
}

object PersistenceMigration {

  def migrate[F[_]: Sync](dbConfig: DatabaseConfig, logger: Logger[F]): F[Unit] =
    for {
      migrationResult <- migrate(dbConfig)
      _               <- logMigrationResult(migrationResult, logger)
    } yield ()

  private def migrate[F[_]: Sync](dbConfig: DatabaseConfig): F[MigrateResult] =
    Sync[F].blocking {
      FlywayMigration.migrate(
        url = dbConfig.url,
        username = dbConfig.user,
        password = dbConfig.password
      )
    }

  private def logMigrationResult[F[_]: Apply](migrateResult: MigrateResult, logger: Logger[F]): F[Unit] = {
    val migrations = migrateResult.migrations.asScala.toList
    val logF       = if (migrations.isEmpty) logEmptyMigration(logger) else logMigration(migrations, logger)
    val divider    = "-" * 50

    logger.info(divider) *>
      logger.info("DATABASE MIGRATION") *>
      logF *>
      logger.info(divider)
  }

  private def logEmptyMigration[F[_]](logger: Logger[F]): F[Unit] =
    logger.info("No migration is applied")

  private def logMigration[F[_]: Apply](migrations: List[MigrateOutput], logger: Logger[F]): F[Unit] =
    logger.info("Applied DB migrations:") *> logger.info(migrationPrettyString(migrations))

  private def migrationPrettyString(migrations: List[MigrateOutput]): String =
    migrations.zipWithIndex
      .map { case (migration, idx) => s"${idx + 1}) ${migration.description} v${migration.version}" }
      .mkString("\n")

}
