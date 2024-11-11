package net.casual.arcade.minigame.template.minigame

import com.mojang.serialization.Codec
import com.mojang.serialization.MapCodec
import net.casual.arcade.minigame.lobby.LobbyMinigame
import net.casual.arcade.minigame.serialization.MinigameFactory
import net.casual.arcade.minigame.utils.MinigameRegistries
import net.casual.arcade.resources.pack.PackInfo
import net.casual.arcade.utils.codec.CodecProvider.Companion.register
import net.minecraft.core.Registry
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerPlayer
import org.jetbrains.annotations.ApiStatus.Experimental
import java.util.function.Function

@Experimental
public interface MinigamesTemplate {
    public val name: String
    public val minigames: List<MinigameFactory>
    public val repeat: Boolean

    public fun createLobby(server: MinecraftServer): LobbyMinigame

    public fun isAdmin(player: ServerPlayer): Boolean

    public fun getAdditionalPacks(): Iterable<PackInfo>

    public fun codec(): MapCodec<out MinigamesTemplate>

    public companion object {
        public val DEFAULT: MinigamesTemplate = SimpleMinigamesTemplate()

        public val CODEC: Codec<MinigamesTemplate> by lazy {
            MinigameRegistries.MINIGAMES_EVENT.byNameCodec()
                .dispatch(MinigamesTemplate::codec, Function.identity())
        }

        internal fun bootstrap(registry: Registry<MapCodec<out MinigamesTemplate>>) {
            SimpleMinigamesTemplate.register(registry)
        }
    }
}

