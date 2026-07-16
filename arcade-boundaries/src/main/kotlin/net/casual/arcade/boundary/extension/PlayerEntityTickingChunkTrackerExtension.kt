/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.boundary.extension

import it.unimi.dsi.fastutil.longs.LongOpenHashSet
import net.casual.arcade.events.GlobalEventHandler
import net.casual.arcade.events.server.player.PlayerClientboundPacketEvent
import net.casual.arcade.events.server.player.PlayerClientboundPacketEvent.Companion.PHASE_POST
import net.casual.arcade.events.server.player.PlayerDimensionChangeEvent
import net.casual.arcade.events.utils.register
import net.casual.arcade.extensions.PlayerExtension
import net.casual.arcade.extensions.event.PlayerExtensionEvent
import net.casual.arcade.extensions.utils.getExtension
import net.minecraft.network.protocol.Packet
import net.minecraft.network.protocol.game.ClientboundChunksBiomesPacket
import net.minecraft.network.protocol.game.ClientboundForgetLevelChunkPacket
import net.minecraft.network.protocol.game.ClientboundLevelChunkWithLightPacket
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.level.ChunkPos

public class PlayerEntityTickingChunkTrackerExtension(player: ServerPlayer): PlayerExtension(player) {
    private val loaded = LongOpenHashSet()

    public fun isLoaded(chunkX: Int, chunkZ: Int): Boolean {
        return this.isLoaded(ChunkPos.pack(chunkX, chunkZ))
    }

    public fun isLoaded(pos: Long): Boolean {
        return this.loaded.contains(pos)
    }

    private fun load(chunkX: Int, chunkZ: Int) {
        this.loaded.add(ChunkPos.pack(chunkX, chunkZ))
    }

    private fun unload(chunkX: Int, chunkZ: Int) {
        this.loaded.remove(ChunkPos.pack(chunkX, chunkZ))
    }

    private fun onClientboundPacket(packet: Packet<*>) {
        // This can run off-thread, but none of these specific packets should be sent off thread in theory
        when (packet) {
            is ClientboundLevelChunkWithLightPacket -> this.load(packet.x, packet.z)
            is ClientboundForgetLevelChunkPacket -> this.unload(packet.pos.x, packet.pos.z)
            is ClientboundChunksBiomesPacket -> packet.chunkBiomeData.forEach { data -> this.load(data.pos.x, data.pos.z) }
        }
    }

    public companion object {
        public val ServerPlayer.entityTickingChunkTrackerExtension: PlayerEntityTickingChunkTrackerExtension
            get() = this.getExtension()

        internal fun registerEvents() {
            GlobalEventHandler.Server.register<PlayerExtensionEvent> {
                it.addExtension(::PlayerEntityTickingChunkTrackerExtension)
            }
            GlobalEventHandler.Server.register<PlayerDimensionChangeEvent> { (player) ->
                player.entityTickingChunkTrackerExtension.loaded.clear()
            }
            GlobalEventHandler.Server.register<PlayerClientboundPacketEvent>(phase = PHASE_POST) { (player, packet) ->
                player.entityTickingChunkTrackerExtension.onClientboundPacket(packet)
            }
        }
    }
}