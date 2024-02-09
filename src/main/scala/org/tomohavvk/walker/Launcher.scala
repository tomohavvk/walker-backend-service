package org.tomohavvk.walker

import cats.effect.ExitCode
import cats.effect.IO
import cats.effect.IOApp
import cats.effect.std.Console
import org.tomohavvk.walker.module.Environment

object Launcher extends IOApp {

  override def run(args: List[String]): IO[ExitCode] =
    Environment.make[IO].flatMap {
      case Right(env)      => runApp(env)
      case Left(exception) => failApp(exception)
    }

  private def runApp(implicit environment: Environment[IO]): IO[ExitCode] =
    new Application[IO].run()

  private def failApp(exception: Exception): IO[ExitCode] =
    Console[IO]
      .error(exception)
      .flatMap(_ => IO.pure(ExitCode.Error))
}
