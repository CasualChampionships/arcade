package net.casual.arcade.minigame.managers.chat

import com.mojang.serialization.Codec
import com.mojang.serialization.MapCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.casual.arcade.minigame.chat.PlayerChatFormatter
import net.casual.arcade.minigame.Minigame
import net.casual.arcade.minigame.managers.MinigameChatManager
import net.casual.arcade.minigame.utils.MinigameRegistries
import net.casual.arcade.utils.ResourceUtils
import net.casual.arcade.utils.codec.CodecProvider
import net.casual.arcade.utils.codec.CodecProvider.Companion.register
import net.minecraft.core.Registry
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerPlayer
import net.minecraft.util.ExtraCodecs
import net.minecraft.world.scores.PlayerTeam
import java.util.*
import java.util.function.Function

public sealed interface MinigameChatMode {
    public val name: Component

    public fun getChatFormatter(manager: MinigameChatManager): PlayerChatFormatter
    
    public fun canSendTo(
        sender: ServerPlayer,
        receiver: ServerPlayer,
        mode: MinigameChatMode?,
        minigame: Minigame<*>
    ): Boolean

    public fun switchedToMessage(receiver: ServerPlayer): Component

    public fun codec(): MapCodec<out MinigameChatMode>

    public fun isEquivalent(mode: MinigameChatMode, sender: ServerPlayer, receiver: ServerPlayer): Boolean {
        return mode == this
    }

    public companion object {
        public val CODEC: Codec<MinigameChatMode> by lazy {
            MinigameRegistries.MINIGAME_CHAT_MODES.byNameCodec()
                .dispatch(MinigameChatMode::codec, Function.identity())
        }

        public val OPTIONAL_CODEC: Codec<Optional<MinigameChatMode>> by lazy {
            ExtraCodecs.optionalEmptyMap(CODEC)
        }

        public fun bootstrap(registry: Registry<MapCodec<out MinigameChatMode>>) {
            Spectator.register(registry)
            Admin.register(registry)
            OwnTeam.register(registry)
            Team.register(registry)
        }
    }

    public data object Global: MinigameChatMode, CodecProvider<Global> {
        override val ID: ResourceLocation = ResourceUtils.arcade("spectator")
        override val CODEC: MapCodec<Global> = MapCodec.unit(Global)

        override val name: Component = Component.translatable("minigame.chat.mode.global")

        override fun getChatFormatter(manager: MinigameChatManager): PlayerChatFormatter {
            return manager.globalChatFormatter
        }

        override fun canSendTo(
            sender: ServerPlayer,
            receiver: ServerPlayer,
            mode: MinigameChatMode?,
            minigame: Minigame<*>
        ): Boolean {
            return minigame.players.has(receiver) || minigame.settings.canCrossChat
        }

        override fun switchedToMessage(receiver: ServerPlayer): Component {
            return Component.translatable("minigame.chat.mode.switch.global")
        }

        override fun codec(): MapCodec<out MinigameChatMode> {
            return CODEC
        }
    }

    public data object Spectator: MinigameChatMode, CodecProvider<Spectator> {
        override val ID: ResourceLocation = ResourceUtils.arcade("spectator")
        override val CODEC: MapCodec<Spectator> = MapCodec.unit(Spectator)

        override val name: Component = Component.translatable("minigame.chat.mode.spectator")

        override fun getChatFormatter(manager: MinigameChatManager): PlayerChatFormatter {
            return manager.spectatorChatFormatter
        }

        override fun canSendTo(
            sender: ServerPlayer,
            receiver: ServerPlayer,
            mode: MinigameChatMode?,
            minigame: Minigame<*>
        ): Boolean {
            return minigame.players.isSpectating(receiver) || mode == this
        }

        override fun switchedToMessage(receiver: ServerPlayer): Component {
            return Component.translatable("minigame.chat.mode.switch.spectator")
        }

        override fun codec(): MapCodec<out MinigameChatMode> {
            return CODEC
        }
    }

    public data object Admin: MinigameChatMode, CodecProvider<Admin> {
        override val ID: ResourceLocation = ResourceUtils.arcade("admin")
        override val CODEC: MapCodec<Admin> = MapCodec.unit(Admin)

        override val name: Component = Component.translatable("minigame.chat.mode.admin")

        override fun getChatFormatter(manager: MinigameChatManager): PlayerChatFormatter {
            return manager.adminChatFormatter
        }

        override fun canSendTo(
            sender: ServerPlayer,
            receiver: ServerPlayer,
            mode: MinigameChatMode?,
            minigame: Minigame<*>
        ): Boolean {
            return minigame.players.isAdmin(receiver) || mode == this
        }

        override fun switchedToMessage(receiver: ServerPlayer): Component {
            return Component.translatable("minigame.chat.mode.switch.admin")
        }

        override fun codec(): MapCodec<out MinigameChatMode> {
            return CODEC
        }
    }

    public data object OwnTeam: MinigameChatMode, CodecProvider<OwnTeam> {
        override val ID: ResourceLocation = ResourceUtils.arcade("own_team")
        override val CODEC: MapCodec<OwnTeam> = MapCodec.unit(OwnTeam)

        override val name: Component = Component.translatable("minigame.chat.mode.team")

        override fun getChatFormatter(manager: MinigameChatManager): PlayerChatFormatter {
            return manager.teamChatFormatter
        }

        override fun canSendTo(
            sender: ServerPlayer,
            receiver: ServerPlayer,
            mode: MinigameChatMode?,
            minigame: Minigame<*>
        ): Boolean {
            val team = receiver.team ?: return false
            return team.isAlliedTo(sender.team) || (mode is Team && team.isAlliedTo(mode.getTeam(sender.server)))
        }

        override fun switchedToMessage(receiver: ServerPlayer): Component {
            return Component.translatable("minigame.chat.mode.switch.ownTeam")
        }

        override fun codec(): MapCodec<out MinigameChatMode> {
            return CODEC
        }
    }

    public class Team private constructor(internal val teamName: String): MinigameChatMode {
        private val formatter = PlayerChatFormatter.createTeamFormatter { this.getTeam(it.server) }

        override val name: Component = OwnTeam.name

        public fun getTeam(server: MinecraftServer): PlayerTeam? {
            return server.scoreboard.getPlayerTeam(this.teamName)
        }

        override fun getChatFormatter(manager: MinigameChatManager): PlayerChatFormatter {
            return this.formatter
        }

        override fun canSendTo(
            sender: ServerPlayer,
            receiver: ServerPlayer,
            mode: MinigameChatMode?,
            minigame: Minigame<*>
        ): Boolean {
            if (mode == this) {
                return true
            }
            val team = this.getTeam(sender.server)
            return team != null && team.isAlliedTo(receiver.team)
        }

        override fun switchedToMessage(receiver: ServerPlayer): Component {
            val team = this.getTeam(receiver.server)
            val name = team?.formattedDisplayName ?: Component.translatable("minigame.chat.mode.switch.unknown")
            return Component.translatable("minigame.chat.mode.switch.specificTeam", name)
        }

        override fun codec(): MapCodec<out MinigameChatMode> {
            return CODEC
        }

        public companion object: CodecProvider<Team> {
            override val ID: ResourceLocation = ResourceUtils.arcade("team")

            override val CODEC: MapCodec<Team> = RecordCodecBuilder.mapCodec { instance ->
                instance.group(
                    Codec.STRING.fieldOf("team_name").forGetter { it.teamName }
                ).apply(instance, ::Team)
            }

            private val cache = WeakHashMap<PlayerTeam, Team>()

            public fun getOrCreate(team: PlayerTeam): Team {
                return this.cache.getOrPut(team) { Team(team.name) }
            }
        }
    }
}