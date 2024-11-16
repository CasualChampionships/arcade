package net.casual.arcade.events.player

import net.minecraft.advancements.AdvancementHolder
import net.minecraft.server.level.ServerPlayer
import kotlin.jvm.optionals.getOrNull

public data class PlayerAdvancementEvent(
    override val player: ServerPlayer,
    val advancement: AdvancementHolder
): PlayerEvent {
    var announce: Boolean = this.advancement.value.display.getOrNull()?.shouldAnnounceChat() ?: false
    var reward: Boolean = true
}