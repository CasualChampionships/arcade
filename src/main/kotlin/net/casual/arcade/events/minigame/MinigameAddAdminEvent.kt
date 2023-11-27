package net.casual.arcade.events.minigame

import net.casual.arcade.minigame.Minigame
import net.minecraft.server.level.ServerPlayer

public data class MinigameAddAdminEvent(
    override val minigame: Minigame<*>,
    val player: ServerPlayer
): MinigameEvent