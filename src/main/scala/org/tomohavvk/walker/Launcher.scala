package org.tomohavvk.walker

import cats.data.EitherT
import cats.effect.ExitCode
import cats.effect.IO
import cats.effect.IOApp
import cats.effect.std.Console
import doobie.implicits._
import io.odin.Level
import io.odin.consoleLogger
import org.tomohavvk.walker.module.Environment
import org.tomohavvk.walker.module.TransactorModule
import org.tomohavvk.walker.persistence.Transactor
import org.tomohavvk.walker.protocol.errors.AppError
import org.tomohavvk.walker.utils.LiftConnectionIO.liftConnectionIOForEitherT

object Launcher extends IOApp {
  private val logger = consoleLogger[IO]().withMinimalLevel(Level.Info)

  override def run(args: List[String]): IO[ExitCode] =
    Environment
      .make[AppEffect, DbEffect, IO]
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
    implicit environment: Environment[AppEffect, DbEffect, IO],
    transactor:           Transactor[AppEffect, DbEffect]
  ): AppEffect[ExitCode] =
    new Application[AppEffect, DbEffect, IO].run()

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
