package org.tomohavvk.walker.persistence

import cats.effect.kernel.MonadCancelThrow
import doobie.hikari.HikariTransactor
import org.tomohavvk.walker.utils.TransactConnectionIO

class PostgresTransactor[F[_], D[_], M[_]](
  xa:         HikariTransactor[M]
)(implicit T: TransactConnectionIO[F, D, M],
  bracket:    MonadCancelThrow[M])
    extends Transactor[F, D] {

  override def withTxn[A](bodyF: => D[A]): F[A] =
    T.transact(bodyF, xa)

}
