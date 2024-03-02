package org.tomohavvk.walker.utils

import cats.data.EitherT
import cats.implicits._
import doobie.free.connection.ConnectionIO
import doobie.implicits._
import org.tomohavvk.walker.utils.LiftConnectionIO.SqlErrorHandler

import java.sql.SQLException

trait LiftConnectionIO[D[_], E] {

  def lift[A](cio: ConnectionIO[A])(implicit errorHandler: SqlErrorHandler[E]): D[A]
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
}
