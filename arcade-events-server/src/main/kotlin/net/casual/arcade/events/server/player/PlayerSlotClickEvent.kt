/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.events.server.player

import it.unimi.dsi.fastutil.ints.Int2ObjectMap
import net.casual.arcade.events.BuiltInEventPhases
import net.casual.arcade.events.common.CancellableEvent
import net.minecraft.network.HashedStack
import net.minecraft.network.protocol.game.ServerboundContainerClickPacket
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.inventory.AbstractContainerMenu
import net.minecraft.world.inventory.ContainerInput

public data class PlayerSlotClickEvent(
    override val player: ServerPlayer,
    public val menu: AbstractContainerMenu,
    public val index: Int,
    public val button: Int,
    public val input: ContainerInput,
    public val containerId: Int,
    public val stateId: Int,
    public val changedSlots: Int2ObjectMap<HashedStack>,
    public val carriedItem: HashedStack
): CancellableEvent.Default(), PlayerEvent {
    @Deprecated("Use this.input instead", ReplaceWith("this.input"))
    public val action: ContainerInput by this::input

    public companion object {
        public const val PHASE_PRE_VALIDATE: String = "pre_validate"

        public const val PHASE_PRE_CLICK: String = BuiltInEventPhases.PRE

        public const val PHASE_POST_CLICK: String = BuiltInEventPhases.POST

        @JvmStatic
        public fun from(player: ServerPlayer, menu: AbstractContainerMenu, packet: ServerboundContainerClickPacket): PlayerSlotClickEvent {
            return PlayerSlotClickEvent(
                player, menu, packet.slotNum.toInt(), packet.buttonNum.toInt(), packet.containerInput,
                packet.containerId, packet.stateId, packet.changedSlots, packet.carriedItem
            )
        }
    }
}