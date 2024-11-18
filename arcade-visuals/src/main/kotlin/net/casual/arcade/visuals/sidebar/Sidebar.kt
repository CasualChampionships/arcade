package net.casual.arcade.visuals.sidebar

import net.casual.arcade.visuals.core.PlayerUI
import net.casual.arcade.visuals.core.TickableUI
import net.casual.arcade.visuals.elements.PlayerSpecificElement
import net.casual.arcade.visuals.extensions.PlayerSidebarExtension.Companion.sidebar
import net.minecraft.network.chat.Component
import net.minecraft.network.protocol.Packet
import net.minecraft.network.protocol.game.ClientGamePacketListener
import net.minecraft.server.level.ServerPlayer
import java.util.function.Consumer

public abstract class Sidebar(title: PlayerSpecificElement<Component>): PlayerUI(), TickableUI {
    public var title: PlayerSpecificElement<Component> = title
        private set

    public fun setTitle(title: PlayerSpecificElement<Component>) {
        this.title = title

        for (player in this.getPlayers()) {
            player.sidebar.setTitle(title.get(player))
        }
    }

    override fun onAddPlayer(player: ServerPlayer) {
        player.sidebar.set(this)
    }

    override fun onRemovePlayer(player: ServerPlayer) {
        player.sidebar.remove()
    }

    override fun resendTo(player: ServerPlayer, sender: Consumer<Packet<ClientGamePacketListener>>) {
        player.sidebar.resend(sender)
    }

    public abstract fun forEachRow(player: ServerPlayer, consumer: (Int, SidebarComponent) -> Unit)

    internal companion object {
        const val MAX_SIZE: Int = 14
    }
}