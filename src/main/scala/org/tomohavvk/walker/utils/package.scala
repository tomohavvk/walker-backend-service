package org.tomohavvk.walker

import cats.data.EitherT
import cats.data.Kleisli
import cats.effect.kernel.Resource
import org.tomohavvk.walker.protocol.errors.AppError

package object utils extends AppResultSyntax with AppErrorSyntax with AnySyntax with LiftFSyntax {
  type AppFlow[F[_], A]           = EitherT[F, AppError, A]
  type ContextFlow[F[_], A]       = Kleisli[AppFlow[F, *], LogContext, A]
  type ResourceEither[F[_], E, A] = Resource[EitherT[F, E, *], A]
}
