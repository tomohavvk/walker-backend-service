package org.tomohavvk.walker.persistence

trait Transactor[F[_], G[_]] {

  def withTxn[A](bodyF: => G[A]): F[A]

}
