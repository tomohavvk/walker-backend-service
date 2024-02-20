package org.tomohavvk.walker

import org.tomohavvk.walker.utils.LiftConnectionIO.SqlErrorHandler
import org.tomohavvk.walker.protocol.errors.AppError
import org.tomohavvk.walker.protocol.errors.DatabaseError
import org.tomohavvk.walker.protocol.errors.GroupIdNotUniqueError
import org.tomohavvk.walker.protocol.errors.GroupPublicIdNotUniqueError
import org.tomohavvk.walker.protocol.errors.UniqueConstraintError
import org.tomohavvk.walker.protocol.errors.ViolatesForeignKeyError

package object persistence {

  private val PostgresUniqueConstraintSQLState: String     = "23505"
  private val ViolatesForeignKeyConstraintSQLState: String = "23503"

  implicit val errorHandler: SqlErrorHandler[AppError] = {
    case ex if ex.getSQLState == PostgresUniqueConstraintSQLState =>
      if (ex.getLocalizedMessage.contains("groups_public_id_uniq_idx"))
        GroupPublicIdNotUniqueError("Group public id should be unique")
      else if (ex.getLocalizedMessage.contains("groups_pkey"))
        GroupIdNotUniqueError("Group id should be unique")
      else
        UniqueConstraintError("Entity already exists", ex.getLocalizedMessage, Some(ex))

    case ex if ex.getSQLState == ViolatesForeignKeyConstraintSQLState =>
      println(2, ex)
      ViolatesForeignKeyError(ex.getLocalizedMessage, Some(ex))

    case ex =>
      println(3, ex)
      new DatabaseError(ex.getLocalizedMessage, Some(ex))

  }

}
