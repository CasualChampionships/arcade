package net.casual.arcade.events.minigame

import net.casual.arcade.minigame.Minigame
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerPlayer

public data class MinigameAddTagEvent(
    override val minigame: Minigame<*>,
    val player: ServerPlayer,
    val tag: ResourceLocation
): MinigameEvent