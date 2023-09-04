package net.casual.arcade.gui.nametag

import eu.pb4.polymer.virtualentity.api.ElementHolder
import net.minecraft.server.level.ServerPlayer
import net.minecraft.server.network.ServerGamePacketListenerImpl
import java.util.function.Predicate

open class PredicatedElementHolder(
    private val predicate: Predicate<ServerPlayer>
): ElementHolder() {
    override fun onTick() {
        this.attachment?.updateCurrentlyTracking(this.watchingPlayers)
    }

    override fun startWatching(connection: ServerGamePacketListenerImpl): Boolean {
        if (this.predicate.test(connection.player)) {
            return super.startWatching(connection)
        }
        this.stopWatching(connection)
        return false
    }
}