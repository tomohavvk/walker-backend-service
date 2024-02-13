package org.tomohavvk

import cats.data.EitherT
import cats.effect.IO
import cats.effect.LiftIO
import cats.~>
import doobie.free.connection.ConnectionIO
import org.tomohavvk.walker.protocol.errors.AppError

package object walker {
  type AppEffect[A] = EitherT[IO, AppError, A]
  type DbEffect[A]  = EitherT[ConnectionIO, AppError, A]

  implicit val LiftMF: IO ~> AppEffect = LiftIO.liftK[AppEffect]

}
