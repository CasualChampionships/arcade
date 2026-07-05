/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.events.server.player

import net.casual.arcade.events.phase.BuiltInEventPhases
import net.casual.arcade.events.common.CancellableEvent
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.inventory.AbstractContainerMenu

public data class PlayerMenuButtonClickEvent(
    override val player: ServerPlayer,
    val menu: AbstractContainerMenu,
    val containerId: Int,
    val buttonId: Int
): CancellableEvent.Default(), PlayerEvent {
    public companion object {
        public const val PHASE_PRE_CLICK: Int = BuiltInEventPhases.PRE

        public const val PHASE_POST_CLICK: Int = BuiltInEventPhases.POST

        public const val PHASE_PRE_VALIDATE: Int = 3
    }
}