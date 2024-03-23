package net.casual.arcade.utils

import net.casual.arcade.events.GlobalEventHandler
import net.casual.arcade.events.player.PlayerCreatedEvent
import net.casual.arcade.events.player.PlayerLeaveEvent
import net.casual.arcade.events.player.PlayerTickEvent
import net.casual.arcade.events.server.ServerLoadedEvent
import net.casual.arcade.gui.extensions.PlayerSidebarExtension
import net.casual.arcade.gui.sidebar.SidebarComponent
import net.casual.arcade.utils.PlayerUtils.addExtension
import net.casual.arcade.utils.PlayerUtils.getExtension
import net.minecraft.ChatFormatting
import net.minecraft.network.chat.Component
import net.minecraft.network.protocol.Packet
import net.minecraft.network.protocol.game.*
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.scores.DisplaySlot
import java.util.function.Consumer

internal object SidebarUtils {
    const val MAX_SIZE = 14

    private const val OBJECTIVE_NAME = "Z\$DummyObjective"
    private val objective = ScoreboardUtils.dummyObjective(OBJECTIVE_NAME)
    private val players = ArrayList<String>(16)

    internal val ServerPlayer.sidebar
        get() = this.getExtension(PlayerSidebarExtension::class.java)


    internal fun sendSetObjectivePacket(
        player: ServerPlayer,
        method: Int,
        title: Component? = null,
        sender: Consumer<Packet<ClientGamePacketListener>> = Consumer(player.connection::send)
    ) {
        if (title !== null) {
            this.objective.displayName = title
        }
        val packet = ClientboundSetObjectivePacket(this.objective, method)
        sender.accept(packet)
    }

    internal fun sendSetScorePacket(
        player: ServerPlayer,
        index: Int,
        component: SidebarComponent,
        sender: Consumer<Packet<ClientGamePacketListener>> = Consumer(player.connection::send)
    ) {
        sender.accept(ClientboundSetScorePacket(
            this.players[index],
            OBJECTIVE_NAME,
            index,
            component.display,
            component.score
        ))
    }

    internal fun sendResetScorePacket(
        player: ServerPlayer,
        index: Int,
        sender: Consumer<Packet<ClientGamePacketListener>> = Consumer(player.connection::send)
    ) {
        sender.accept(ClientboundResetScorePacket(
            OBJECTIVE_NAME,
            this.players[index]
        ))
    }

    internal fun sendSetSidebarDisplayPacket(
        player: ServerPlayer,
        remove: Boolean,
        sender: Consumer<Packet<ClientGamePacketListener>> = Consumer(player.connection::send)
    ) {
        sender.accept(ClientboundSetDisplayObjectivePacket(
            DisplaySlot.SIDEBAR,
            if (remove) null else this.objective
        ))
    }

    internal fun registerEvents() {
        GlobalEventHandler.register<ServerLoadedEvent> {
            for (i in 0..15) {
                val player = ChatFormatting.RESET.toString().repeat(i)
                this.players.add(player)
            }
        }
        GlobalEventHandler.register<PlayerCreatedEvent> { (player) ->
            player.addExtension(PlayerSidebarExtension(player.connection))
        }
        GlobalEventHandler.register<PlayerLeaveEvent> { (player) ->
            player.sidebar.disconnect()
        }
        GlobalEventHandler.register<PlayerTickEvent> { (player) ->
            player.sidebar.tick()
        }
    }
}