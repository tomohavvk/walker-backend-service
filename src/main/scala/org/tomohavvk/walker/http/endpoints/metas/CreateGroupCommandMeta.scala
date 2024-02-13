package org.tomohavvk.walker.http.endpoints.metas

import org.tomohavvk.walker.protocol.Types.TraceId
import org.tomohavvk.walker.protocol.commands.CreateGroupCommand

case class CreateGroupCommandMeta(traceId: TraceId, command: CreateGroupCommand)
