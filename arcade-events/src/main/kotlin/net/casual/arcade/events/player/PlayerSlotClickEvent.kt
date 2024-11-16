package net.casual.arcade.events.player

import net.casual.arcade.events.BuiltInEventPhases
import net.casual.arcade.events.core.CancellableEvent
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.inventory.AbstractContainerMenu
import net.minecraft.world.inventory.ClickType as ClickAction

public data class PlayerSlotClickEvent(
    override val player: ServerPlayer,
    val menu: AbstractContainerMenu,
    val index: Int,
    val button: Int,
    val action: ClickAction
): CancellableEvent.Default(), PlayerEvent {
    public companion object {
        public const val PHASE_PRE: String = BuiltInEventPhases.PRE

        public const val PHASE_POST: String = BuiltInEventPhases.POST
    }
}