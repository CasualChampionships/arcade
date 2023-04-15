package net.casualuhc.arcade.events.player

import net.casualuhc.arcade.events.core.Event
import net.minecraft.advancements.Advancement
import net.minecraft.server.level.ServerPlayer

data class PlayerAdvancementEvent(
    val player: ServerPlayer,
    val advancement: Advancement
): Event() {
    var announce = this.advancement.display?.shouldAnnounceChat() ?: false
}