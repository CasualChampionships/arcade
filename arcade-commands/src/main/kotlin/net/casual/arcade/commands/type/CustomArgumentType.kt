package net.casual.arcade.commands.type

import com.mojang.brigadier.StringReader
import com.mojang.brigadier.arguments.ArgumentType
import com.mojang.brigadier.arguments.StringArgumentType.StringType.QUOTABLE_PHRASE
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.suggestion.SuggestionProvider
import com.mojang.brigadier.suggestion.Suggestions
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import net.casual.arcade.commands.mixins.ArgumentTypeInfosAccessor
import net.minecraft.commands.SharedSuggestionProvider
import net.minecraft.commands.synchronization.SuggestionProviders
import java.util.concurrent.CompletableFuture

public abstract class CustomArgumentType<T>: ArgumentType<T> {
    init {
        CLASS_MAP.computeIfAbsent(this::class.java) { this.getArgumentInfo() }
    }

    abstract override fun parse(reader: StringReader): T

    override fun <S> parse(reader: StringReader, source: S): T {
        return super.parse(reader, source)
    }

    override fun <S> listSuggestions(
        context: CommandContext<S>,
        builder: SuggestionsBuilder
    ): CompletableFuture<Suggestions> {
        return super.listSuggestions(context, builder)
    }

    public fun getSuggestionProvider(): SuggestionProvider<SharedSuggestionProvider>? {
        return SuggestionProviders.ASK_SERVER
    }

    public open fun getArgumentInfo(): CustomArgumentTypeInfo<*> {
        return CustomStringArgumentInfo(QUOTABLE_PHRASE)
    }

    private companion object {
        private val CLASS_MAP = ArgumentTypeInfosAccessor.getClassMap()
    }
}