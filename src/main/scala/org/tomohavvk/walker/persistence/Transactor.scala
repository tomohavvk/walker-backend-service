package org.tomohavvk.walker.persistence

trait Transactor[F[_], D[_]] {

  def withTxn[A](bodyF: => D[A]): F[A]

}
