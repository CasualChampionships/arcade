package net.casual.arcade.events.player

import net.minecraft.advancements.Advancement
import net.minecraft.server.level.ServerPlayer
import kotlin.jvm.optionals.getOrNull

public data class PlayerAdvancementEvent(
    override val player: ServerPlayer,
    val advancement: Advancement
): PlayerEvent {
    var announce: Boolean = this.advancement.display.getOrNull()?.shouldAnnounceChat() ?: false
}