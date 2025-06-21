/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.events.server.player

import net.casual.arcade.events.common.CancellableEvent
import net.casual.arcade.events.server.level.LocatedLevelEvent
import net.minecraft.core.BlockPos
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.state.BlockState

public data class PlayerBlockMinedEvent(
    override val player: ServerPlayer,
    override val pos: BlockPos,
    val state: BlockState,
    val entity: BlockEntity?
): CancellableEvent.Default(), PlayerEvent, LocatedLevelEvent {
    override val level: ServerLevel
        get() = this.player.level()
}