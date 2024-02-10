package org.tomohavvk.walker

import org.tomohavvk.walker.utils.LiftConnectionIO.SqlErrorHandler
import org.tomohavvk.walker.protocol.errors.AlreadyExistError
import org.tomohavvk.walker.protocol.errors.AppError
import org.tomohavvk.walker.protocol.errors.DatabaseError

package object persistence {

  private val PostgresUniqueConstraintSQLState: String = "23505"

  implicit val errorHandler: SqlErrorHandler[AppError] = {
    case ex if ex.getSQLState == PostgresUniqueConstraintSQLState =>
      AlreadyExistError(ex.getLocalizedMessage, Some(ex))
    case ex =>
      DatabaseError(ex.getLocalizedMessage, Some(ex))

  }

}
