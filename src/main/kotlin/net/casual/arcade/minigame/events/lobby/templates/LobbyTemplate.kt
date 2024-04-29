package net.casual.arcade.minigame.events.lobby.templates

import com.mojang.serialization.Codec
import net.casual.arcade.minigame.events.lobby.Lobby
import net.casual.arcade.utils.registries.ArcadeRegistries
import net.casual.arcade.utils.serialization.CodecProvider.Companion.register
import net.minecraft.core.Registry
import net.minecraft.server.level.ServerLevel
import java.util.function.Function

public interface LobbyTemplate {
    public fun create(level: ServerLevel): Lobby

    public fun codec(): Codec<out LobbyTemplate>

    public companion object {
        public val DEFAULT: LobbyTemplate = SimpleLobbyTemplate()

        public val CODEC: Codec<LobbyTemplate> by lazy {
            ArcadeRegistries.LOBBY_TEMPLATE.byNameCodec()
                .dispatch(LobbyTemplate::codec, Function.identity())
        }

        internal fun bootstrap(registry: Registry<Codec<out LobbyTemplate>>) {
            SimpleLobbyTemplate.register(registry)
        }
    }
}