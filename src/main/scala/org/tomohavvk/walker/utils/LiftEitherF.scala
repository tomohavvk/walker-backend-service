package org.tomohavvk.walker.utils

import cats.Monad
import cats.data.EitherT
import cats.effect.IO

trait LiftEitherF[F[_], E] {

  def fromEither[A](either: Either[E, A]): F[A]

  def fromEitherF[A](fe: F[Either[E, A]]): F[A]

}

object LiftEitherF {

  implicit def liftEitherForEitherT[F[_]: Monad, E]: LiftEitherF[EitherT[F, E, *], E] =
    new LiftEitherF[EitherT[F, E, *], E] {

      override def fromEither[A](either: Either[E, A]): EitherT[F, E, A] =
        EitherT.fromEither[F](either)

      override def fromEitherF[A](fe: EitherT[F, E, Either[E, A]]): EitherT[F, E, A] =
        fe.flatMap(fromEither)
    }

  implicit def liftEitherForIO: LiftEitherF[IO[*], Throwable] =
    new LiftEitherF[IO[*], Throwable] {

      override def fromEither[A](either: Either[Throwable, A]): IO[A] =
        IO.fromEither(either)

      override def fromEitherF[A](fe: IO[Either[Throwable, A]]): IO[A] =
        fe.flatMap(fromEither)

    }

}
