package net.casual.arcade.gui.tab

import net.casual.arcade.gui.PlayerUI
import net.casual.arcade.gui.suppliers.ComponentSupplier
import net.casual.arcade.utils.TabUtils.tabDisplay
import net.minecraft.network.protocol.Packet
import net.minecraft.network.protocol.game.ClientGamePacketListener
import net.minecraft.server.level.ServerPlayer
import java.util.function.Consumer

public class ArcadeTabDisplay(header: ComponentSupplier, footer: ComponentSupplier): PlayerUI() {
    public var header: ComponentSupplier = header
        private set
    public var footer: ComponentSupplier = footer
        private set

    public fun setDisplay(header: ComponentSupplier, footer: ComponentSupplier) {
        this.header = header
        this.footer = footer

        for (player in this.getPlayers()) {
            player.tabDisplay.setDisplay(header.getComponent(player), footer.getComponent(player))
        }
    }

    override fun onAddPlayer(player: ServerPlayer) {
        player.tabDisplay.set(this)
    }

    override fun onRemovePlayer(player: ServerPlayer) {
        player.tabDisplay.remove()
    }

    override fun resendTo(player: ServerPlayer, sender: Consumer<Packet<ClientGamePacketListener>>) {
        player.tabDisplay.resend(sender)
    }
}