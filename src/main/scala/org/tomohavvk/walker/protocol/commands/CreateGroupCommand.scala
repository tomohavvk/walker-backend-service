package org.tomohavvk.walker.protocol.commands

import org.tomohavvk.walker.protocol.Types.GroupName
import org.tomohavvk.walker.protocol.Types.IsPublic

case class CreateGroupCommand(name: GroupName, isPublic: IsPublic)
