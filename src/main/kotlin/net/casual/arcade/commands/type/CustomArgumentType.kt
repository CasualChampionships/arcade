package net.casual.arcade.commands.type

import com.mojang.brigadier.arguments.StringArgumentType.StringType.QUOTABLE_PHRASE
import com.mojang.brigadier.suggestion.SuggestionProvider
import net.casual.arcade.mixin.commands.ArgumentTypeInfosAccessor
import net.minecraft.commands.SharedSuggestionProvider
import net.minecraft.commands.synchronization.ArgumentTypeInfo
import net.minecraft.commands.synchronization.SuggestionProviders

abstract class CustomArgumentType {
    init {
        CLASS_MAP.computeIfAbsent(this::class.java) { this.getArgumentInfo() }
    }

    fun getSuggestionProvider(): SuggestionProvider<SharedSuggestionProvider>? {
        return SuggestionProviders.ASK_SERVER
    }

    fun getArgumentInfo(): ArgumentTypeInfo<*, *> {
        return CustomStringArgumentInfo(QUOTABLE_PHRASE)
    }

    companion object {
        private val CLASS_MAP = ArgumentTypeInfosAccessor.getClassMap()
    }
}