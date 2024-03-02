package org.tomohavvk.walker.module

import cats.effect.Async
import cats.effect.Resource
import cats.effect.kernel.MonadCancelThrow
import com.zaxxer.hikari.HikariConfig
import doobie.hikari.HikariTransactor
import doobie.util.ExecutionContexts
import doobie.util.log.LogHandler.jdkLogHandler
import org.tomohavvk.walker.config.DatabaseConfig
import org.tomohavvk.walker.persistence.PostgresTransactor
import org.tomohavvk.walker.persistence.Transactor
import org.tomohavvk.walker.utils.TransactConnectionIO

case class TransactorDeps[F[_], B[_]](transactor: Transactor[F, B])

object TransactorModule {

  def make[F[_]: Async, D[_], M[_]: Async: MonadCancelThrow](
    configs:    Configs
  )(implicit T: TransactConnectionIO[F, D, M]
  ): Resource[M, TransactorDeps[F, D]] =
    for {
      transactor <- makeTransactor[M](configs.database)
      postgresTransactor = new PostgresTransactor[F, D, M](transactor)
    } yield TransactorDeps[F, D](postgresTransactor)

  private def makeTransactor[F[_]: Async](config: DatabaseConfig): Resource[F, HikariTransactor[F]] =
    for {
      connectionPool <- ExecutionContexts.fixedThreadPool(config.maximumPoolSize)
      hikariTransactor <-
        HikariTransactor.fromHikariConfigCustomEc[F](hikariConfig(config), connectionPool, Some(jdkLogHandler))
    } yield hikariTransactor

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

}
