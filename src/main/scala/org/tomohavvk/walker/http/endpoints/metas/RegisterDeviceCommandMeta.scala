package org.tomohavvk.walker.http.endpoints.metas

import org.tomohavvk.walker.protocol.Types.XAuthDeviceId
import org.tomohavvk.walker.protocol.commands.RegisterDeviceCommand

case class RegisterDeviceCommandMeta(authenticatedDeviceId: XAuthDeviceId, command: RegisterDeviceCommand)
