package net.casual.arcade.minigame.template.lobby

import com.mojang.serialization.Codec
import com.mojang.serialization.MapCodec
import net.casual.arcade.minigame.lobby.Lobby
import net.casual.arcade.minigame.utils.MinigameRegistries
import net.casual.arcade.utils.codec.CodecProvider.Companion.register
import net.minecraft.core.Registry
import net.minecraft.server.level.ServerLevel
import java.util.function.Function

public interface LobbyTemplate {
    public fun create(level: ServerLevel): Lobby

    public fun codec(): MapCodec<out LobbyTemplate>

    public companion object {
        public val DEFAULT: LobbyTemplate = SimpleLobbyTemplate()

        public val CODEC: Codec<LobbyTemplate> by lazy {
            MinigameRegistries.LOBBY_TEMPLATE.byNameCodec()
                .dispatch(LobbyTemplate::codec, Function.identity())
        }

        internal fun bootstrap(registry: Registry<MapCodec<out LobbyTemplate>>) {
            SimpleLobbyTemplate.register(registry)
        }
    }
}