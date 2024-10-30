package net.casual.arcade.minigame.events

import net.casual.arcade.minigame.Minigame
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerPlayer

public data class MinigameRemoveTagEvent(
    override val minigame: Minigame,
    val player: ServerPlayer,
    val tag: ResourceLocation
): MinigameEvent