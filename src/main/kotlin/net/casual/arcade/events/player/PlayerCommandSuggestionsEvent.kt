package net.casual.arcade.events.player

import com.mojang.brigadier.suggestion.Suggestions
import net.casual.arcade.events.core.CancellableEvent
import net.minecraft.server.level.ServerPlayer
import java.util.concurrent.CompletableFuture

public data class PlayerCommandSuggestionsEvent(
    override val player: ServerPlayer,
    val command: String
): CancellableEvent.Typed<CompletableFuture<Suggestions>>(), PlayerEvent