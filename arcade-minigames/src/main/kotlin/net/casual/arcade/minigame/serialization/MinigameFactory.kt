package net.casual.arcade.minigame.serialization

import com.mojang.serialization.Codec
import com.mojang.serialization.MapCodec
import net.casual.arcade.minigame.Minigame
import net.casual.arcade.minigame.lobby.LobbyMinigameFactory
import net.casual.arcade.minigame.utils.MinigameRegistries
import net.casual.arcade.utils.codec.CodecProvider.Companion.register
import net.minecraft.core.Registry
import java.util.function.Function

public interface MinigameFactory {
    public fun create(context: MinigameCreationContext): Minigame

    public fun codec(): MapCodec<out MinigameFactory>

    public companion object {
        public val CODEC: Codec<MinigameFactory> = Codec.lazyInitialized {
            MinigameRegistries.MINIGAME_FACTORY.byNameCodec()
                .dispatch(MinigameFactory::codec, Function.identity())
        }

        internal fun bootstrap(registry: Registry<MapCodec<out MinigameFactory>>) {
            LobbyMinigameFactory.register(registry)
        }
    }
}