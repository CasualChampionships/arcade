package net.casual.arcade.minigame.managers.chat

import com.mojang.serialization.Codec
import com.mojang.serialization.MapCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.casual.arcade.Arcade
import net.casual.arcade.chat.PlayerChatFormatter
import net.casual.arcade.minigame.Minigame
import net.casual.arcade.minigame.managers.MinigameChatManager
import net.casual.arcade.utils.registries.ArcadeRegistries
import net.casual.arcade.utils.serialization.CodecProvider
import net.casual.arcade.utils.serialization.CodecProvider.Companion.register
import net.minecraft.core.Registry
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.scores.PlayerTeam
import java.util.WeakHashMap
import java.util.function.Function

public sealed interface MinigameChatMode {
    public fun getChatFormatter(manager: MinigameChatManager): PlayerChatFormatter
    
    public fun canSendTo(receiver: ServerPlayer, sender: ServerPlayer, minigame: Minigame<*>): Boolean

    public fun codec(): MapCodec<out MinigameChatMode>

    public companion object {
        public val CODEC: Codec<MinigameChatMode> by lazy {
            ArcadeRegistries.MINIGAME_CHAT_MODES.byNameCodec()
                .dispatch(MinigameChatMode::codec, Function.identity())
        }

        public fun bootstrap(registry: Registry<MapCodec<out MinigameChatMode>>) {
            Spectator.register(registry)
            Admin.register(registry)
            OwnTeam.register(registry)
            Team.register(registry)
        }
    }

    public data object Spectator: MinigameChatMode, CodecProvider<Spectator> {
        override val ID: ResourceLocation = Arcade.id("spectator")
        override val CODEC: MapCodec<Spectator> = MapCodec.unit(Spectator)

        override fun getChatFormatter(manager: MinigameChatManager): PlayerChatFormatter {
            return manager.spectatorChatFormatter
        }

        override fun canSendTo(receiver: ServerPlayer, sender: ServerPlayer, minigame: Minigame<*>): Boolean {
            return minigame.players.isSpectating(receiver)
        }

        override fun codec(): MapCodec<out MinigameChatMode> {
            return CODEC
        }
    }

    public data object Admin: MinigameChatMode, CodecProvider<Admin> {
        override val ID: ResourceLocation = Arcade.id("admin")
        override val CODEC: MapCodec<Admin> = MapCodec.unit(Admin)

        override fun getChatFormatter(manager: MinigameChatManager): PlayerChatFormatter {
            return manager.adminChatFormatter
        }

        override fun canSendTo(receiver: ServerPlayer, sender: ServerPlayer, minigame: Minigame<*>): Boolean {
            return minigame.players.isAdmin(receiver)
        }

        override fun codec(): MapCodec<out MinigameChatMode> {
            return CODEC
        }
    }

    public data object OwnTeam: MinigameChatMode, CodecProvider<OwnTeam> {
        override val ID: ResourceLocation = Arcade.id("own_team")
        override val CODEC: MapCodec<OwnTeam> = MapCodec.unit(OwnTeam)

        override fun getChatFormatter(manager: MinigameChatManager): PlayerChatFormatter {
            return manager.teamChatFormatter
        }

        override fun canSendTo(receiver: ServerPlayer, sender: ServerPlayer, minigame: Minigame<*>): Boolean {
            return receiver.team?.isAlliedTo(sender.team) ?: false
        }

        override fun codec(): MapCodec<out MinigameChatMode> {
            return CODEC
        }
    }

    public class Team private constructor(private val team: PlayerTeam): MinigameChatMode {
        private val formatter = PlayerChatFormatter.createTeamFormatter { this.team }

        override fun getChatFormatter(manager: MinigameChatManager): PlayerChatFormatter {
            return this.formatter
        }

        override fun canSendTo(receiver: ServerPlayer, sender: ServerPlayer, minigame: Minigame<*>): Boolean {
            return this.team.isAlliedTo(receiver.team)
        }

        override fun codec(): MapCodec<out MinigameChatMode> {
            return CODEC
        }

        public companion object: CodecProvider<Team> {
            override val ID: ResourceLocation = Arcade.id("team")

            override val CODEC: MapCodec<Team> = RecordCodecBuilder.mapCodec { instance ->
                instance.group(
                    Codec.STRING.fieldOf("team_name").forGetter { it.team.name }
                ).apply(instance) { name -> getOrCreate(Arcade.getServer().scoreboard.getPlayerTeam(name)!!) }
            }

            private val cache = WeakHashMap<PlayerTeam, Team>()

            public fun getOrCreate(team: PlayerTeam): Team {
                return this.cache.getOrPut(team) { Team(team) }
            }
        }
    }
}