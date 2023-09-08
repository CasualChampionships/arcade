package net.casual.arcade.resources

import net.casual.arcade.extensions.Extension
import net.minecraft.network.protocol.game.ClientboundResourcePackPacket

internal class PlayerPackExtension: Extension {
    internal var previous: PackInfo? = null
        private set
    internal var current: PackInfo? = null
        private set
    internal var status = PackStatus.WAITING

    internal fun onSentPack(packet: ClientboundResourcePackPacket) {
        if (this.status == PackStatus.SUCCESS) {
            this.previous = this.current
        }
        this.current = PackInfo(packet.url, packet.hash, packet.isRequired, packet.prompt)
        this.status = PackStatus.WAITING
    }
}