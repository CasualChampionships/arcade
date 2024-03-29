package net.casual.arcade.events.player

import com.mojang.brigadier.StringReader
import com.mojang.brigadier.suggestion.Suggestions
import net.minecraft.server.level.ServerPlayer
import java.util.concurrent.CompletableFuture

public data class PlayerCommandSuggestionsEvent(
    override val player: ServerPlayer,
    val command: String
): PlayerEvent {
    private val suggestions = ArrayList<CompletableFuture<Suggestions>>()

    public fun createCommandReader(): StringReader {
        val reader = StringReader(this.command)
        if (reader.canRead() && reader.peek() == '/') {
            reader.skip()
        }
        return reader
    }

    public fun addSuggestions(suggestions: CompletableFuture<Suggestions>) {
        this.suggestions.add(suggestions)
    }

    public fun getAllSuggestions(): List<CompletableFuture<Suggestions>> {
        return this.suggestions
    }
}