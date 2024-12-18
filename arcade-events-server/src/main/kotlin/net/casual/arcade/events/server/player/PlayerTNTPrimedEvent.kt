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

public data class PlayerTNTPrimedEvent(
    override val player: ServerPlayer,
    /**
     * The [ServerLevel] where the TNT is being primed.
     * This is not necessarily the same as [player]'s level.
     */
    override val level: ServerLevel,
    override val pos: BlockPos
): CancellableEvent.Default(), PlayerEvent, LocatedLevelEvent