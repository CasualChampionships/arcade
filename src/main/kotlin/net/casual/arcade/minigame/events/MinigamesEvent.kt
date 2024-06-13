package net.casual.arcade.minigame.events

import com.mojang.serialization.Codec
import com.mojang.serialization.MapCodec
import net.casual.arcade.minigame.events.lobby.LobbyMinigame
import net.casual.arcade.resources.PackInfo
import net.casual.arcade.utils.registries.ArcadeRegistries
import net.casual.arcade.utils.serialization.CodecProvider.Companion.register
import net.minecraft.core.Registry
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerPlayer
import org.jetbrains.annotations.ApiStatus.Experimental
import java.util.function.Function

@Experimental
public interface MinigamesEvent {
    public val name: String
    public val minigames: List<MinigameData>
    public val repeat: Boolean

    public fun createLobby(server: MinecraftServer): LobbyMinigame

    public fun isAdmin(player: ServerPlayer): Boolean

    public fun getAdditionalPacks(): Iterable<PackInfo>

    public fun codec(): MapCodec<out MinigamesEvent>

    public companion object {
        public val DEFAULT: MinigamesEvent = SimpleMinigamesEvent()

        public val CODEC: Codec<MinigamesEvent> by lazy {
            ArcadeRegistries.MINIGAMES_EVENT.byNameCodec()
                .dispatch(MinigamesEvent::codec, Function.identity())
        }

        internal fun bootstrap(registry: Registry<MapCodec<out MinigamesEvent>>) {
            SimpleMinigamesEvent.register(registry)
        }
    }
}

