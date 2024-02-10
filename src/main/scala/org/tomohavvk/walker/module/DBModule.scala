package org.tomohavvk.walker.module

import cats.effect.Async
import cats.effect.Resource
import com.zaxxer.hikari.HikariConfig
import doobie.hikari.HikariTransactor
import doobie.util.ExecutionContexts
import org.tomohavvk.walker.config.DatabaseConfig

case class DBDeps[F[_]](transactor: HikariTransactor[F])

object DBModule {

  def make[F[_]: Async](configs: Configs): Resource[F, DBDeps[F]] =
    for {
      transactor <- makeTransactor(configs.database)

    } yield DBDeps[F](transactor)

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
