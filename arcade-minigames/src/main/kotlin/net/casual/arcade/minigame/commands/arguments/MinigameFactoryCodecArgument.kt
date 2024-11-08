package net.casual.arcade.minigame.commands.arguments

import com.mojang.brigadier.StringReader
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType
import com.mojang.brigadier.suggestion.Suggestions
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import com.mojang.serialization.MapCodec
import net.casual.arcade.commands.type.CustomArgumentType
import net.casual.arcade.commands.type.CustomArgumentTypeInfo
import net.casual.arcade.minigame.serialization.MinigameFactory
import net.casual.arcade.minigame.utils.MinigameRegistries
import net.casual.arcade.utils.ComponentUtils.literal
import net.minecraft.commands.SharedSuggestionProvider
import net.minecraft.commands.arguments.ResourceLocationArgument
import net.minecraft.resources.ResourceLocation
import java.util.concurrent.CompletableFuture
import kotlin.jvm.optionals.getOrNull

public class MinigameFactoryCodecArgument: CustomArgumentType<MapCodec<out MinigameFactory>>() {
    override fun parse(reader: StringReader): MapCodec<out MinigameFactory> {
        val id = ResourceLocation.read(reader)
        return MinigameRegistries.MINIGAME_FACTORY.getOptional(id).getOrNull()
            ?: throw INVALID_FACTORY.create()
    }

    override fun getArgumentInfo(): CustomArgumentTypeInfo<*> {
        return CustomArgumentTypeInfo.of(ResourceLocationArgument::class.java)
    }

    override fun <S: Any?> listSuggestions(
        context: CommandContext<S>,
        builder: SuggestionsBuilder
    ): CompletableFuture<Suggestions> {
        return SharedSuggestionProvider.suggestResource(MinigameRegistries.MINIGAME_FACTORY.keySet(), builder)
    }

    public companion object {
        public val INVALID_FACTORY: SimpleCommandExceptionType = SimpleCommandExceptionType("Invalid Minigame Factory".literal())

        @JvmStatic
        public fun codec(): MinigameFactoryCodecArgument {
            return MinigameFactoryCodecArgument()
        }

        @JvmStatic
        public fun getCodec(context: CommandContext<*>, string: String): MapCodec<out MinigameFactory> {
            @Suppress("UNCHECKED_CAST")
            return context.getArgument(string, MapCodec::class.java) as MapCodec<out MinigameFactory>
        }
    }
}