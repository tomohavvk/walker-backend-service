package org.tomohavvk.walker

import cats.data.EitherT
import cats.effect.std.Console
import cats.effect.ExitCode
import cats.effect.IO
import cats.effect.IOApp
import cats.effect.LiftIO
import cats.~>
import doobie.free.connection.ConnectionIO
import doobie.hikari.HikariTransactor
import io.odin.Level
import io.odin.consoleLogger
import org.tomohavvk.walker.module.{DBModule, Environment}
import org.tomohavvk.walker.persistence.Transactor
import org.tomohavvk.walker.utils.LiftConnectionIO.liftConnectionIOForEitherT
import org.tomohavvk.walker.protocol.errors.AppError

object Launcher extends IOApp {
  private val logger = consoleLogger[IO]().withMinimalLevel(Level.Info)

  type AppEffect[A] = EitherT[IO, AppError, A]
  type DbEffect[A]  = EitherT[ConnectionIO, AppError, A]

  implicit val LiftMF: IO ~> AppEffect = LiftIO.liftK[AppEffect]

  override def run(args: List[String]): IO[ExitCode] = {


    Environment
      .make[AppEffect]
      .flatMap {
        case Right(env)      =>

        EitherT.liftF[IO, AppError, ExitCode](DBModule.make[IO](env.configs).use { dbDeps =>


            runApp(env, dbDeps.transactor)
              .value
              .flatMap {
                case Right(exitCode) => IO(exitCode)
                case Left(error) => logger.error(error.logMessage.value) >> IO(ExitCode.Error)
              }
          })



        case Left(exception) => failApp(exception)
      }.value
      .flatMap {
        case Right(exitCode) => IO(exitCode)
        case Left(error) => logger.error(error.logMessage.value) >> IO(ExitCode.Error)
      }

  }

  private def runApp(implicit environment: Environment[AppEffect], transactor: HikariTransactor[IO]): AppEffect[ExitCode] =
    new Application[AppEffect, DbEffect, IO].run()

  private def failApp(exception: Exception): AppEffect[ExitCode] =
    EitherT.liftF(
      Console[IO]
        .error(exception)
        .as(ExitCode.Error)
    )
}
