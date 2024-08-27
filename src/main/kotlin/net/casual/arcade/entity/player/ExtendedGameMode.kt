package net.casual.arcade.entity.player

import eu.pb4.sgui.virtual.VirtualScreenHandlerInterface
import net.casual.arcade.events.GlobalEventHandler
import net.casual.arcade.events.player.*
import net.casual.arcade.utils.NetworkUtils
import net.casual.arcade.utils.PlayerUtils.getExtension
import net.minecraft.network.protocol.Packet
import net.minecraft.network.protocol.game.ClientGamePacketListener
import net.minecraft.network.protocol.game.ClientboundBundlePacket
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.InteractionResult
import net.minecraft.world.level.GameType
import org.jetbrains.annotations.ApiStatus.Internal

public enum class ExtendedGameMode {
    Survival {
        override fun set(player: ServerPlayer) {
            player.setGameMode(GameType.SURVIVAL)
        }
    },
    Creative {
        override fun set(player: ServerPlayer) {
            player.setGameMode(GameType.CREATIVE)
        }
    },
    Adventure {
        override fun set(player: ServerPlayer) {
            player.setGameMode(GameType.ADVENTURE)
        }
    },
    NoClipSpectator {
        override fun set(player: ServerPlayer) {
            player.setGameMode(GameType.SPECTATOR)
        }
    },
    AdventureSpectator {
        override fun set(player: ServerPlayer) {
            player.setGameMode(GameType.ADVENTURE)
            player.abilities.mayfly = true
            player.abilities.flying = true
            player.abilities.invulnerable = true
            player.onUpdateAbilities()
        }
    };

    internal abstract fun set(player: ServerPlayer)

    public companion object {
        @JvmStatic
        public var ServerPlayer.extendedGameMode: ExtendedGameMode
            get() = this.getExtension(ExtendedGameModePlayerExtension::class.java).getGameMode()
            set(value) = this.getExtension(ExtendedGameModePlayerExtension::class.java).setGameMode(value)

        public fun fromVanilla(type: GameType): ExtendedGameMode {
            return when (type) {
                GameType.SURVIVAL -> Survival
                GameType.CREATIVE -> Creative
                GameType.ADVENTURE -> Adventure
                GameType.SPECTATOR -> NoClipSpectator
            }
        }

        @Internal
        @JvmStatic
        public fun setGameModeFromVanilla(player: ServerPlayer, type: GameType) {
            player.getExtension(ExtendedGameModePlayerExtension::class.java).setGameModeFromVanilla(type)
        }

        internal fun registerEvents() {
            GlobalEventHandler.register<PlayerExtensionEvent> {
                it.addExtension(ExtendedGameModePlayerExtension(it.player))
            }
        }
    }
}