package org.tomohavvk.walker.http.endpoints.metas

import org.tomohavvk.walker.protocol.Types.XAuthDeviceId
import org.tomohavvk.walker.protocol.commands.CreateGroupCommand

case class CreateGroupCommandMeta(authenticatedDeviceId: XAuthDeviceId, command: CreateGroupCommand)
