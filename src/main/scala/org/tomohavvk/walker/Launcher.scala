package org.tomohavvk.walker

import cats.data.EitherT
import cats.effect.std.Console
import cats.effect.ExitCode
import cats.effect.IO
import cats.effect.IOApp
import cats.effect.LiftIO
import cats.~>
import doobie.free.connection.ConnectionIO
import io.odin.Level
import io.odin.consoleLogger
import org.tomohavvk.walker.module.TransactorModule
import org.tomohavvk.walker.module.Environment
import org.tomohavvk.walker.persistence.Transactor
import org.tomohavvk.walker.utils.LiftConnectionIO.liftConnectionIOForEitherT
import org.tomohavvk.walker.protocol.errors.AppError

object Launcher extends IOApp {
  private val logger = consoleLogger[IO]().withMinimalLevel(Level.Info)

  type AppEffect[A] = EitherT[IO, AppError, A]
  type DbEffect[A]  = EitherT[ConnectionIO, AppError, A]

  implicit val LiftMF: IO ~> AppEffect = LiftIO.liftK[AppEffect]

  override def run(args: List[String]): IO[ExitCode] =
    Environment
      .make[AppEffect]
      .flatMap {
        case Right(env) =>
          EitherT.liftF[IO, AppError, ExitCode](TransactorModule.make[AppEffect, DbEffect, IO](env.configs).use {
            txDeps => runApp(env, txDeps.transactor).value.flatMap(handleResult)
          })

        case Left(exception) => failApp(exception)
      }
      .value
      .flatMap(handleResult)

  private def runApp(
    implicit environment: Environment[AppEffect],
    transactor:           Transactor[AppEffect, DbEffect]
  ): AppEffect[ExitCode] =
    new Application[AppEffect, DbEffect].run()

  private def handleResult: Either[AppError, ExitCode] => IO[ExitCode] = {
    case Right(exitCode) => IO(exitCode)
    case Left(error)     => logger.error(error.logMessage.value) >> IO(ExitCode.Error)
  }

  private def failApp(exception: Exception): AppEffect[ExitCode] =
    EitherT.liftF(
      Console[IO]
        .error(exception)
        .as(ExitCode.Error)
    )
}
