package net.casual.arcade.commands.type

import com.mojang.brigadier.arguments.ArgumentType
import com.mojang.brigadier.arguments.StringArgumentType.StringType.QUOTABLE_PHRASE
import com.mojang.brigadier.suggestion.SuggestionProvider
import net.casual.arcade.mixin.commands.ArgumentTypeInfosAccessor
import net.minecraft.commands.SharedSuggestionProvider
import net.minecraft.commands.synchronization.SuggestionProviders

public abstract class CustomArgumentType<T>: ArgumentType<T> {
    init {
        CLASS_MAP.computeIfAbsent(this::class.java) { this.getArgumentInfo() }
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