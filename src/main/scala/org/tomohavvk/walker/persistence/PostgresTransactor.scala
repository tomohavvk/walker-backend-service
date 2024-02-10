package org.tomohavvk.walker.persistence

import cats.effect.kernel.MonadCancelThrow
import doobie.hikari.HikariTransactor
import org.tomohavvk.walker.utils.TransactConnectionIO

class PostgresTransactor[F[_], G[_], M[_]](
  xa:         HikariTransactor[M]
)(implicit T: TransactConnectionIO[F, G, M],
  bracket:    MonadCancelThrow[M])
    extends Transactor[F, G] {

  override def withTxn[A](bodyF: => G[A]): F[A] =
    T.transact(bodyF, xa)

}
