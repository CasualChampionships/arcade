package net.casual.arcade.commands.arguments

import com.mojang.brigadier.StringReader
import com.mojang.brigadier.arguments.ArgumentType
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.suggestion.Suggestions
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import net.casual.arcade.commands.type.CustomArgumentType
import net.minecraft.commands.SharedSuggestionProvider
import java.time.ZoneId
import java.util.concurrent.CompletableFuture

public class TimeZoneArgument: CustomArgumentType<ZoneId>() {
    override fun parse(reader: StringReader): ZoneId {
        return ZoneId.of(reader.readUnquotedString())
    }

    override fun <S: Any> listSuggestions(context: CommandContext<S>, builder: SuggestionsBuilder): CompletableFuture<Suggestions> {
        return SharedSuggestionProvider.suggest(ZoneId.getAvailableZoneIds().map { "\"$it\"" }, builder)
    }

    public companion object {
        @JvmStatic
        public fun timeZone(): TimeZoneArgument {
            return TimeZoneArgument()
        }

        @JvmStatic
        public fun getTimeZone(context: CommandContext<*>, string: String): ZoneId {
            return context.getArgument(string, ZoneId::class.java)
        }
    }
}