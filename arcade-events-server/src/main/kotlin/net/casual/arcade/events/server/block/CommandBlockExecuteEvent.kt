/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.events.server.block

import net.casual.arcade.events.common.CancellableEvent
import net.casual.arcade.events.server.level.LocatedLevelEvent
import net.minecraft.commands.CommandSourceStack
import net.minecraft.core.BlockPos
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.level.BaseCommandBlock

public data class CommandBlockExecuteEvent(
    override val level: ServerLevel,
    val commandBlock: BaseCommandBlock,
    val source: CommandSourceStack,
    val command: String
): CancellableEvent.Default(), LocatedLevelEvent {
    override val pos: BlockPos
        get() = BlockPos.containing(this.commandBlock.position)
}