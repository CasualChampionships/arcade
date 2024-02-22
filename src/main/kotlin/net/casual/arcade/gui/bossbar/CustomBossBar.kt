package net.casual.arcade.gui.bossbar

import net.casual.arcade.gui.PlayerUI
import net.casual.arcade.utils.BossbarUtils.bossbars
import net.minecraft.network.protocol.Packet
import net.minecraft.network.protocol.game.ClientGamePacketListener
import net.minecraft.network.protocol.game.ClientboundBossEventPacket
import net.minecraft.server.level.ServerPlayer
import net.minecraft.util.Mth
import java.util.*
import java.util.function.Consumer

/**
 * A custom boss bar implementation that can be
 * displayed to players individually by providing
 * the boss bar on a per-player basis.
 *
 * Each of the components of the boss bar is
 * updated as per the [interval] set. By default,
 * this is set to `1` which updates all the components
 * **every** tick.
 *
 * You can inherit this class to implement the
 * abstract methods, or you can use [SuppliedBossBar]
 * and provide suppliers for the components
 * of the boss bar.
 *
 * Once you have a [CustomBossBar] implementation
 * you can simply call [addPlayer] to display
 * the boss bar to them. You may also [removePlayer]
 * or [clearPlayers], for more information see [PlayerUI].
 *
 * @see SuppliedBossBar
 * @see PlayerUI
 */
public abstract class CustomBossBar: PlayerUI(), BossBarSupplier {
    internal val uuid: UUID = Mth.createInsecureUUID()

    final override fun onAddPlayer(player: ServerPlayer) {
        player.bossbars.add(this)
    }

    final override fun onRemovePlayer(player: ServerPlayer) {
        player.bossbars.remove(this)
    }

    override fun resendTo(player: ServerPlayer, sender: Consumer<Packet<ClientGamePacketListener>>) {
        val event = player.bossbars.getEvent(this) ?: return
        sender.accept(ClientboundBossEventPacket.createAddPacket(event))
    }
}