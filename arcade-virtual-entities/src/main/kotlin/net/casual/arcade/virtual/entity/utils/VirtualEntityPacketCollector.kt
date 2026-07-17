/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.virtual.entity.utils

import it.unimi.dsi.fastutil.ints.IntArrayList
import it.unimi.dsi.fastutil.objects.ObjectArrayList
import it.unimi.dsi.fastutil.objects.ObjectListIterator
import net.casual.arcade.utils.asClientGamePacket
import net.casual.arcade.utils.network.PacketSender
import net.minecraft.network.protocol.BundlerInfo
import net.minecraft.network.protocol.Packet
import net.minecraft.network.protocol.game.ClientGamePacketListener
import net.minecraft.network.protocol.game.ClientboundBundlePacket
import net.minecraft.network.protocol.game.ClientboundRemoveEntitiesPacket

public class VirtualEntityPacketCollector {
    private val collected = ObjectArrayList<Packet<*>>()

    public fun add(packet: Packet<*>): VirtualEntityPacketCollector {
        this.collected.add(packet)
        return this
    }

    public fun optimize(): VirtualEntityPacketCollector {
        if (this.collected.size <= 1) {
            return this
        }

        val optimized = ObjectArrayList<Packet<*>>()
        val iterator = this.collected.listIterator()
        for (packet in iterator) {
            optimized.add(this.tryOptimizePacket(packet, iterator))
        }

        this.collected.clear()
        this.collected.addAll(optimized)
        return this
    }

    public fun bundle(): VirtualEntityPacketCollector {
        if (this.collected.size <= 1) {
            return this
        }

        val bundled = ObjectArrayList<ClientboundBundlePacket>()

        var bundle = ObjectArrayList<Packet<ClientGamePacketListener>>()
        for (packet in this.collected) {
            if (packet is ClientboundBundlePacket) {
                if (bundle.isNotEmpty()) {
                    bundled.add(ClientboundBundlePacket(bundle))
                    bundle = ObjectArrayList<Packet<ClientGamePacketListener>>()
                }

                bundled.add(packet)
                continue
            }

            bundle.add(packet.asClientGamePacket())
            if (bundle.size == BundlerInfo.BUNDLE_SIZE_LIMIT) {
                bundled.add(ClientboundBundlePacket(bundle))
                bundle = ObjectArrayList<Packet<ClientGamePacketListener>>()
            }
        }
        if (bundle.isNotEmpty()) {
            bundled.add(ClientboundBundlePacket(bundle))
        }

        this.collected.clear()
        this.collected.addAll(bundled)
        return this
    }

    public fun send(consumer: PacketSender) {
        for (packet in this.collected) {
            consumer.send(packet)
        }
    }

    private fun tryOptimizePacket(packet: Packet<*>, iterator: ObjectListIterator<Packet<*>>): Packet<*> {
        if (packet is ClientboundRemoveEntitiesPacket) {
            val removed = IntArrayList(packet.entityIds)
            while (iterator.hasNext()) {
                val next = iterator.next()
                if (next !is ClientboundRemoveEntitiesPacket) {
                    iterator.back(1)
                    break
                }
                removed.addAll(next.entityIds)
            }
            return ClientboundRemoveEntitiesPacket(removed)
        }
        return packet
    }
}