package org.tomohavvk.walker.utils

import cats.data.EitherT

trait UnliftF[F[_], M[_], E] {
  def unlift[A](fa: F[A]): M[Either[E, A]]
}

object UnliftF {

  implicit def unliftFForEitherT[M[_], E]: UnliftF[EitherT[M, E, *], M, E] =
    new UnliftF[EitherT[M, E, *], M, E] {

      override def unlift[A](fa: EitherT[M, E, A]): M[Either[E, A]] =
        fa.value
    }

}
