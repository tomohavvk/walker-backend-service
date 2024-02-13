package org.tomohavvk.walker.utils

import cats.data.EitherT
import cats.effect.kernel.MonadCancelThrow
import doobie.free.connection.ConnectionIO
import doobie._
import doobie.implicits._

trait TransactConnectionIO[F[_], G[_], M[_]] {
  def transact[A](ga: G[A], xa: Transactor[M])(implicit M: MonadCancelThrow[M]): F[A]
}

object TransactConnectionIO {

  implicit def transactConnectionIOForEitherT[
    M[_],
    E
  ]: TransactConnectionIO[EitherT[M, E, *], EitherT[ConnectionIO, E, *], M] =
    new TransactConnectionIO[EitherT[M, E, *], EitherT[ConnectionIO, E, *], M] {

      override def transact[A](
        ga:         EitherT[ConnectionIO, E, A],
        xa:         Transactor[M]
      )(implicit M: MonadCancelThrow[M]
      ): EitherT[M, E, A] =
        EitherT.apply[M, E, A](ga.value.transact(xa))
    }
}
