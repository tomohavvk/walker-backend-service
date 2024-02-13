package org.tomohavvk.walker.protocol.commands

import org.tomohavvk.walker.protocol.Types.GroupName
import org.tomohavvk.walker.protocol.Types.IsPrivate

case class CreateGroupCommand(name: GroupName, isPrivate: IsPrivate)
