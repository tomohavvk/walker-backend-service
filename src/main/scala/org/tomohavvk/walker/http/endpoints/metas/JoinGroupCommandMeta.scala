package org.tomohavvk.walker.http.endpoints.metas

import org.tomohavvk.walker.protocol.Types.GroupId
import org.tomohavvk.walker.protocol.Types.XAuthDeviceId

case class JoinGroupCommandMeta(authenticatedDeviceId: XAuthDeviceId, groupId: GroupId)
