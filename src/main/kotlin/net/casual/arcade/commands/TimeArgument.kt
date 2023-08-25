package net.casual.arcade.commands

import com.mojang.brigadier.StringReader
import com.mojang.brigadier.arguments.ArgumentType
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.suggestion.Suggestions
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.SharedSuggestionProvider
import java.time.LocalTime
import java.util.concurrent.CompletableFuture

class TimeArgument: ArgumentType<LocalTime>, CustomArgumentType {
    override fun parse(reader: StringReader): LocalTime {
        return LocalTime.parse(reader.readString())
    }

    override fun <S: Any> listSuggestions(context: CommandContext<S>, builder: SuggestionsBuilder): CompletableFuture<Suggestions> {
        return SharedSuggestionProvider.suggest(hours, builder)
    }

    companion object {
        private val hours = ArrayList<String>()

        init {
            for (i in 0..23) {
                this.hours.add("\"%02d:00\"".format(i))
            }
        }

        fun time(): TimeArgument {
            return TimeArgument()
        }

        fun getTime(context: CommandContext<CommandSourceStack>, string: String): LocalTime {
            return context.getArgument(string, LocalTime::class.java)
        }
    }
}