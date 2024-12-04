package net.casual.arcade.events.block

import net.casual.arcade.events.core.CancellableEvent
import net.casual.arcade.events.level.LocatedLevelEvent
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