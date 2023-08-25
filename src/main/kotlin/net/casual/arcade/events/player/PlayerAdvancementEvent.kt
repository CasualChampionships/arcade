package net.casual.arcade.events.player

import net.minecraft.advancements.Advancement
import net.minecraft.server.level.ServerPlayer

data class PlayerAdvancementEvent(
    override val player: ServerPlayer,
    val advancement: Advancement
): PlayerEvent {
    var announce = this.advancement.display?.shouldAnnounceChat() ?: false
}