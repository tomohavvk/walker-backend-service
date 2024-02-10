package org.tomohavvk.walker.utils

import cats.Applicative
import cats.data.EitherT
import cats.data.Kleisli
import org.tomohavvk.walker.protocol.errors.AppError

trait AnySyntax {

  implicit def anySyntax[A](a: A): AnyOps[A] =
    new AnyOps(a)
}

final class AnyOps[A](private val a: A) extends AnyVal {

  def rightT[F[_]: Applicative, AA >: A]: ContextFlow[F, AA] =
    Kleisli.liftF[AppFlow[F, *], LogContext, AA](EitherT.rightT[F, AppError](a))

  def pureEitherT[F[_]: Applicative, AA >: A]: AppFlow[F, AA] =
    EitherT.rightT[F, AppError](a)
}

trait LiftFSyntax {

  implicit def liftFSyntax[F[_]: Applicative, A](a: F[A]): LiftFOps[F, A] =
    new LiftFOps(a)
}

final class LiftFOps[F[_]: Applicative, A](private val a: F[A]) {

  def liftF: AppFlow[F, A] =
    EitherT.liftF[F, AppError, A](a)

  def liftFlow: ContextFlow[F, A] =
    EitherT.liftF[F, AppError, A](a).liftFlow
}

trait AppResultSyntax {

  implicit def appResultSyntax[F[_], A](appResult: AppFlow[F, A]): AppResultOps[F, A] =
    new AppResultOps(appResult)
}

final class AppResultOps[F[_], A](private val appResult: AppFlow[F, A]) extends AnyVal {
  def liftFlow: ContextFlow[F, A] = Kleisli.liftF[AppFlow[F, *], LogContext, A](appResult)
}

trait AppErrorSyntax {

  implicit def appErrorSyntax(appError: AppError): AppErrorOps =
    new AppErrorOps(appError)
}

final class AppErrorOps(private val appError: AppError) extends AnyVal {

  def leftT[F[_]: Applicative, A]: ContextFlow[F, A] =
    Kleisli.liftF[AppFlow[F, *], LogContext, A](EitherT.leftT[F, A](appError))
}
