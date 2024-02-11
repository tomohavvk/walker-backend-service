package org.tomohavvk.walker.utils

import cats.Monad
import cats.data.EitherT
import cats.implicits._
import cats.mtl.Raise
import doobie.free.connection.ConnectionIO
import doobie.implicits._
import org.tomohavvk.walker.utils.LiftConnectionIO.FromOptionFPartiallyApplied
import org.tomohavvk.walker.utils.LiftConnectionIO.SqlErrorHandler

import java.sql.SQLException

trait LiftConnectionIO[F[_], E] {

  def lift[A](cio: ConnectionIO[A])(implicit errorHandler: SqlErrorHandler[E]): F[A]

  def fromOptionF(ifNone: => E): FromOptionFPartiallyApplied[F, E] =
    new FromOptionFPartiallyApplied[F, E](ifNone)(this)

}

object LiftConnectionIO {

  type SqlErrorHandler[E] = SQLException => E

  implicit def liftConnectionIOForEitherT[E]: LiftConnectionIO[EitherT[ConnectionIO, E, *], E] =
    new LiftConnectionIO[EitherT[ConnectionIO, E, *], E] {

      override def lift[A](
        cio:                   ConnectionIO[A]
      )(implicit errorHandler: SqlErrorHandler[E]
      ): EitherT[ConnectionIO, E, A] =
        EitherT.apply(cio.attemptSql.map(_.leftMap(errorHandler)))

    }

  class FromOptionFPartiallyApplied[F[_], E](ifNone: => E)(L: LiftConnectionIO[F, E]) {

    def apply[A](
      cioOpt:       ConnectionIO[Option[A]]
    )(implicit F:   Monad[F],
      R:            Raise[F, E],
      errorHandler: SqlErrorHandler[E]
    ): F[A] =
      L.lift(cioOpt).flatMap {
        case Some(value) => F.pure(value)
        case _           => R.raise(ifNone)
      }

  }

}
