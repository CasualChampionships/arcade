package net.casual.arcade.minigame.events

import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.casual.arcade.Arcade
import net.casual.arcade.events.minigame.LobbyMoveToNextMinigameEvent
import net.casual.arcade.events.minigame.SequentialMinigameStartEvent
import net.casual.arcade.minigame.Minigame
import net.casual.arcade.minigame.MinigameResources.Companion.sendTo
import net.casual.arcade.minigame.Minigames
import net.casual.arcade.minigame.events.lobby.LobbyMinigame
import net.casual.arcade.minigame.serialization.MinigameCreationContext
import net.casual.arcade.utils.EventUtils.broadcast
import net.casual.arcade.utils.MinigameUtils.transferAdminAndSpectatorTeamsTo
import net.casual.arcade.utils.ResourcePackUtils.sendResourcePack
import net.minecraft.core.UUIDUtil
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerPlayer
import org.jetbrains.annotations.ApiStatus.Experimental
import java.util.*
import kotlin.jvm.optionals.getOrNull

@Experimental
public class SequentialMinigames(
    public var event: MinigamesEvent,
    public val server: MinecraftServer
) {
    private var current: Minigame<*>? = null
    private var lobby: LobbyMinigame? = null
    public var index: Int = 0

    public fun getCurrent(): Minigame<*> {
        if (this.current == null) {
            this.returnToLobby()
        }
        return this.current!!
    }

    public fun getNext(): Minigame<*>? {
        val current = this.getCurrent()
        if (current === this.lobby) {
            return current.nextMinigame
        }
        return this.lobby
    }

    public fun addPlayer(player: ServerPlayer) {
        val current = this.getCurrent()
        // We start sending packs before player is added this
        // way listeners can hook into the #afterPacksLoad event
        this.sendResourcesTo(player, false)
        current.players.add(player, admin = this.event.isAdmin(player))
    }

    public fun startNewMinigame(minigame: Minigame<*>) {
        val current = this.current
        this.current = minigame
        if (current != null) {
            if (current === minigame) {
                throw IllegalArgumentException("Cannot start current minigame!")
            }

            current.transferAdminAndSpectatorTeamsTo(minigame)
            current.players.transferTo(minigame)
            current.close()
        }

        this.incrementIndex(minigame)

        minigame.start()
        SequentialMinigameStartEvent(minigame, this).broadcast()
    }

    public fun returnToLobby() {
        if (this.current != null && this.current === this.lobby) {
            return
        }

        var lobby = this.lobby
        if (lobby == null) {
            lobby = this.event.createLobby(this.server)
            this.setLobby(lobby)
        }

        this.startNewMinigame(lobby)

        val next = this.createNextMinigame()
        if (next != null) {
            lobby.nextMinigame = next
        }
    }

    public fun reloadLobby() {
        val lobby = this.lobby
        if (lobby != null) {
            this.lobby = null
            if (this.current != null && this.current === lobby) {
                this.returnToLobby()
            } else {
                lobby.close()
            }
        }
    }

    public fun sendResourcesTo(player: ServerPlayer, minigame: Boolean = true) {
        for (pack in this.event.getAdditionalPacks()) {
            player.sendResourcePack(pack)
        }
        if (minigame) {
            this.getCurrent().getResources().sendTo(player)
        }
    }

    public fun getNextMinigameData(): MinigameData? {
        val minigames = this.event.minigames

        if (this.index in minigames.indices) {
            return this.event.minigames[this.index]
        }
        if (this.event.repeat && minigames.isNotEmpty()) {
            this.index = 0
            return this.getNextMinigameData()
        }
        return null
    }

    public fun setData(data: Data) {
        this.index = data.currentIndex

        val minigame = Minigames.get(data.currentUUID)
        val lobby = data.currentLobbyUUID.map { Minigames.get(it) as? LobbyMinigame }.orElse(null)

        if (lobby != null) {
            this.setLobby(lobby)
        }
        if (minigame != null) {
            this.startNewMinigame(minigame)
        } else if (lobby != null) {
            this.returnToLobby()
        } else {
            return
        }

        // This must be re-set after
        this.index = data.currentIndex
    }

    public fun getData(): Data {
        val current = this.getCurrent()
        return Data(
            current.uuid,
            Optional.ofNullable(lobby?.uuid),
            current.id,
            this.index
        )
    }

    private fun setLobby(lobby: LobbyMinigame) {
        this.lobby = lobby

        lobby.events.register<LobbyMoveToNextMinigameEvent> {
            this.incrementIndex(it.next)
            this.current = it.next
        }
    }

    private fun createNextMinigame(): Minigame<*>? {
        val (minigameId, customData) = this.getNextMinigameData() ?: return null
        val factory = Minigames.getFactory(minigameId)
        if (factory == null) {
            Arcade.logger.error("Failed to create next minigame, non-existent factory")
            return null
        }
        return factory.create(MinigameCreationContext(this.server, customData.getOrNull()))
    }

    private fun incrementIndex(next: Minigame<*>) {
        val data = this.getNextMinigameData()
        if (data != null && next.id == data.id) {
            this.index++
        }
    }

    public data class Data(
        val currentUUID: UUID,
        val currentLobbyUUID: Optional<UUID>,
        val currentId: ResourceLocation,
        val currentIndex: Int
    ) {
        public companion object {
            public val CODEC: Codec<Data> = RecordCodecBuilder.create { instance ->
                instance.group(
                    UUIDUtil.CODEC.fieldOf("current_uuid").forGetter(Data::currentUUID),
                    UUIDUtil.CODEC.optionalFieldOf("current_lobby_uuid").forGetter(Data::currentLobbyUUID),
                    ResourceLocation.CODEC.fieldOf("current_id").forGetter(Data::currentId),
                    Codec.INT.fieldOf("current_index").forGetter(Data::currentIndex)
                ).apply(instance, ::Data)
            }
        }
    }
}