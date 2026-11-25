/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.minigame.utils

import it.unimi.dsi.fastutil.ints.IntArrayList
import net.casual.arcade.events.GlobalEventHandler
import net.casual.arcade.events.server.network.ClientboundPacketEvent
import net.casual.arcade.events.server.player.PlayerClientboundPacketEvent
import net.casual.arcade.events.server.registry.RegistryEventHandler
import net.casual.arcade.events.utils.register
import net.casual.arcade.minigame.utils.MinigameUtils.getMinigame
import net.casual.arcade.utils.arcade
import net.minecraft.core.Holder
import net.minecraft.core.registries.Registries
import net.minecraft.network.protocol.common.ClientboundUpdateTagsPacket
import net.minecraft.network.protocol.game.ClientboundSetTimePacket
import net.minecraft.resources.ResourceKey
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerPlayer
import net.minecraft.tags.TagNetworkSerialization
import net.minecraft.world.attribute.EnvironmentAttributes
import net.minecraft.world.attribute.modifier.FloatModifier
import net.minecraft.world.clock.ClockNetworkState
import net.minecraft.world.clock.WorldClock
import net.minecraft.world.timeline.Timeline
import java.util.*
import kotlin.jvm.optionals.getOrNull

// FIXME: I think the packet manip is wrong, but I cba to test it rn - Sensei
internal object GameMusicSuppression {
    private val ID = arcade("music_suppression")
    private val CLOCK = ResourceKey.create(Registries.WORLD_CLOCK, ID)
    private val TIMELINE = ResourceKey.create(Registries.TIMELINE, ID)

    private val SUPPRESSED = ClockNetworkState(0, 0.0F, 0.0F)
    private val UNSUPPRESSED = ClockNetworkState(1, 0.0F, 0.0F)

    fun registerEvents() {
        RegistryEventHandler.register(Registries.WORLD_CLOCK) { event ->
            event.register(CLOCK, WorldClock())
        }
        RegistryEventHandler.register(Registries.TIMELINE) { event ->
            val clock = event.lookupOrThrow(Registries.WORLD_CLOCK).getOrThrow(CLOCK)
            val timeline = Timeline.builder(clock)
                .addModifierTrack(EnvironmentAttributes.MUSIC_VOLUME, FloatModifier.MULTIPLY) { track ->
                    track.addKeyframe(0, 0.0F).addKeyframe(1, 1.0F)
                }
                .build()
            event.register(TIMELINE, timeline)
        }

        GlobalEventHandler.Server.register<ClientboundPacketEvent> { event ->
            val packet = event.packet
            if (packet is ClientboundUpdateTagsPacket) {
                event.packet = addTimelineToTags(event.server, packet)
            }
        }
        GlobalEventHandler.Server.register<PlayerClientboundPacketEvent> { event ->
            val packet = event.packet
            if (packet is ClientboundSetTimePacket) {
                event.packet = replaceClockState(event.player, packet)
            }
        }
    }

    fun send(player: ServerPlayer, suppressed: Boolean) {
        val server = player.level().server
        val state = if (suppressed) SUPPRESSED else UNSUPPRESSED
        player.connection.send(ClientboundSetTimePacket(server.overworld().gameTime, mapOf(clock(server) to state)))
    }

    private fun clock(server: MinecraftServer): Holder<WorldClock> {
        return server.registryAccess().lookupOrThrow(Registries.WORLD_CLOCK).getOrThrow(CLOCK)
    }

    private fun replaceClockState(player: ServerPlayer, packet: ClientboundSetTimePacket): ClientboundSetTimePacket {
        val clock = clock(player.level().server)
        val state = packet.clockUpdates[clock]
        if (state == null || state === SUPPRESSED || state === UNSUPPRESSED) {
            return packet
        }
        val suppressed = player.getMinigame()?.music?.isSuppressingGameMusic() == true
        val updates = HashMap(packet.clockUpdates)
        updates[clock] = if (suppressed) SUPPRESSED else UNSUPPRESSED
        return ClientboundSetTimePacket(packet.gameTime, updates)
    }

    private fun addTimelineToTags(server: MinecraftServer, packet: ClientboundUpdateTagsPacket): ClientboundUpdateTagsPacket {
        val payload = packet.tags[Registries.TIMELINE] ?: return packet
        val registries = server.registryAccess()
        val timelines = registries.lookupOrThrow(Registries.TIMELINE)
        val id = timelines.getId(timelines.getValueOrThrow(TIMELINE))

        val tags = HashMap(payload.tags)
        for (dimension in registries.lookupOrThrow(Registries.DIMENSION_TYPE)) {
            val tag = dimension.timelines().unwrapKey().getOrNull() ?: continue
            val ids = tags[tag.location()] ?: continue
            if (!ids.contains(id)) {
                val copy = IntArrayList(ids)
                copy.add(id)
                tags[tag.location()] = copy
            }
        }

        val updated = IdentityHashMap(packet.tags)
        updated[Registries.TIMELINE] = TagNetworkSerialization.NetworkPayload(tags)
        return ClientboundUpdateTagsPacket(updated)
    }
}
