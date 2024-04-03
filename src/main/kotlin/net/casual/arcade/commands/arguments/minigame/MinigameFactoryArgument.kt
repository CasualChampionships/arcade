package net.casual.arcade.commands.arguments.minigame

import com.mojang.brigadier.StringReader
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType
import com.mojang.brigadier.suggestion.Suggestions
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import net.casual.arcade.commands.type.CustomArgumentType
import net.casual.arcade.commands.type.CustomArgumentTypeInfo
import net.casual.arcade.minigame.Minigames
import net.casual.arcade.minigame.serialization.MinigameFactory
import net.casual.arcade.utils.ComponentUtils.literal
import net.minecraft.commands.SharedSuggestionProvider
import net.minecraft.commands.arguments.ResourceLocationArgument
import net.minecraft.resources.ResourceLocation
import java.util.concurrent.CompletableFuture

public class MinigameFactoryArgument: CustomArgumentType<MinigameFactory>() {
    override fun parse(reader: StringReader): MinigameFactory {
        val id = ResourceLocation.read(reader)
        return Minigames.getFactory(id) ?: throw INVALID_FACTORY.create()
    }

    override fun getArgumentInfo(): CustomArgumentTypeInfo<*> {
        return CustomArgumentTypeInfo.of(ResourceLocationArgument::class.java)
    }

    override fun <S: Any?> listSuggestions(
        context: CommandContext<S>,
        builder: SuggestionsBuilder
    ): CompletableFuture<Suggestions> {
        return SharedSuggestionProvider.suggestResource(Minigames.getAllFactoryIds(), builder)
    }

    public companion object {
        public val INVALID_FACTORY: SimpleCommandExceptionType = SimpleCommandExceptionType("Invalid Minigame Factory".literal())

        @JvmStatic
        public fun factory(): MinigameFactoryArgument {
            return MinigameFactoryArgument()
        }

        @JvmStatic
        public fun getFactory(context: CommandContext<*>, string: String): MinigameFactory {
            return context.getArgument(string, MinigameFactory::class.java)
        }
    }
}