package org.tomohavvk.walker

import org.tomohavvk.walker.utils.LiftConnectionIO.SqlErrorHandler
import org.tomohavvk.walker.protocol.errors.UniqueConstraintError
import org.tomohavvk.walker.protocol.errors.AppError
import org.tomohavvk.walker.protocol.errors.DatabaseError
import org.tomohavvk.walker.protocol.errors.ViolatesForeignKeyError

package object persistence {

  private val PostgresUniqueConstraintSQLState: String     = "23505"
  private val ViolatesForeignKeyConstraintSQLState: String = "23503"

  implicit val errorHandler: SqlErrorHandler[AppError] = {
    case ex if ex.getSQLState == PostgresUniqueConstraintSQLState =>
      UniqueConstraintError("Entity already exists", ex.getLocalizedMessage, Some(ex))

    case ex if ex.getSQLState == ViolatesForeignKeyConstraintSQLState =>
      ViolatesForeignKeyError(ex.getLocalizedMessage, Some(ex))

    case ex =>
      new DatabaseError(ex.getLocalizedMessage, Some(ex))

  }

}
