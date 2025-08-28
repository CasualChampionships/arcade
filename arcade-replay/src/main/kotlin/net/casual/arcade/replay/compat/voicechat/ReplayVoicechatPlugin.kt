/*
 * Copyright (c) 2025 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.replay.compat.voicechat

import de.maxhenkel.voicechat.Voicechat
import de.maxhenkel.voicechat.api.VoicechatApi
import de.maxhenkel.voicechat.api.VoicechatConnection
import de.maxhenkel.voicechat.api.VoicechatPlugin
import de.maxhenkel.voicechat.api.events.*
import de.maxhenkel.voicechat.api.opus.OpusDecoder
import de.maxhenkel.voicechat.api.packets.SoundPacket
import de.maxhenkel.voicechat.net.*
import de.maxhenkel.voicechat.plugins.impl.VolumeCategoryImpl
import net.casual.arcade.events.GlobalEventHandler
import net.casual.arcade.events.ListenerRegistry.Companion.register
import net.casual.arcade.replay.events.chunk.ReplayChunkRecorderSnapshotEvent
import net.casual.arcade.replay.events.player.ReplayPlayerRecorderSnapshotEvent
import net.casual.arcade.replay.io.ReplayFormat
import net.casual.arcade.replay.recorder.ReplayRecorder
import net.casual.arcade.replay.recorder.chunk.ReplayChunkRecorders
import net.casual.arcade.replay.recorder.player.ReplayPlayerRecorders
import net.casual.arcade.utils.ArcadeUtils
import net.casual.arcade.utils.EnumUtils
import net.casual.arcade.utils.PlayerUtils.levelServer
import net.minecraft.Util
import net.minecraft.network.protocol.Packet
import net.minecraft.network.protocol.common.ClientCommonPacketListener
import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket
import net.minecraft.server.level.ServerPlayer
import org.jetbrains.annotations.ApiStatus.Internal

@Internal
public object ReplayVoicechatPlugin: VoicechatPlugin {
    private val cache = VoicechatPacketCache()

    private lateinit var decoder: OpusDecoder

    override fun getPluginId(): String {
        return "${ArcadeUtils.MOD_ID}_replay_recorder"
    }

    override fun initialize(api: VoicechatApi) {
        decoder = api.createDecoder()

        GlobalEventHandler.Server.register<ReplayPlayerRecorderSnapshotEvent>(this::onPlayerRecorderSnapshot)
        GlobalEventHandler.Server.register<ReplayChunkRecorderSnapshotEvent>(this::onChunkRecorderSnapshot)
    }

    override fun registerEvents(registration: EventRegistration) {
        registration.registerEvent(LocationalSoundPacketEvent::class.java, this::onLocationalSoundPacket, -100_000)
        registration.registerEvent(EntitySoundPacketEvent::class.java, this::onEntitySoundPacket, -100_000)
        registration.registerEvent(StaticSoundPacketEvent::class.java, this::onStaticSoundPacket, -100_000)
        registration.registerEvent(MicrophonePacketEvent::class.java, this::onMicrophonePacket, -100_000)

        registration.registerEvent(RegisterVolumeCategoryEvent::class.java, this::onRegisterCategory)
        registration.registerEvent(UnregisterVolumeCategoryEvent::class.java, this::onUnregisterCategory)
        registration.registerEvent(PlayerStateChangedEvent::class.java, this::onPlayerStateChanged)
    }

    private fun onLocationalSoundPacket(event: LocationalSoundPacketEvent) {
        if (event.isCancelled) {
            return
        }

        val packet = event.packet
        val converter = event.voicechat.audioConverter
        this.recordForReceiver(event) { format ->
            cache.getOrCreate(format, decoder, converter, packet)
        }
    }

    private fun onEntitySoundPacket(event: EntitySoundPacketEvent) {
        if (event.isCancelled) {
            return
        }

        val packet = event.packet
        val converter = event.voicechat.audioConverter
        this.recordForReceiver(event) { format ->
            cache.getOrCreate(format, decoder, converter, packet)
        }
    }

    private fun onStaticSoundPacket(event: StaticSoundPacketEvent) {
        if (event.isCancelled) {
            return
        }

        val packet = event.packet
        val converter = event.voicechat.audioConverter
        this.recordForReceiver(event) { format ->
            cache.getOrCreate(format, decoder, converter, packet)
        }
    }

    private fun onMicrophonePacket(event: MicrophonePacketEvent) {
        val connection = event.senderConnection ?: return
        val player = connection.getServerPlayer() ?: return
        val server = player.levelServer
        val converter = event.voicechat.audioConverter
        val grouped = connection.isInGroup
        val distance = event.voicechat.voiceChatDistance.toFloat()

        server.execute {
            val map = EnumUtils.mapOf<ReplayFormat, Packet<ClientCommonPacketListener>>()
            val lazy: (ReplayFormat) -> Packet<ClientCommonPacketListener> = { format ->
                map.getOrPut(format) {
                    this.cache.create(format, decoder, converter, event.packet, player.uuid, grouped, distance)
                }
            }

            for (recorder in ReplayPlayerRecorders.get(player)) {
                recorder.recordIfVoicechatEnabled { lazy.invoke(recorder.format) }
            }

            if (!grouped) {
                val dimension = player.level().dimension()
                val chunkPos = player.chunkPosition()
                for (recorder in ReplayChunkRecorders.containing(dimension, chunkPos)) {
                    recorder.recordIfVoicechatEnabled { lazy.invoke(recorder.format) }
                }
            }
        }
    }

    private fun onRegisterCategory(event: RegisterVolumeCategoryEvent) {
        val server = Voicechat.SERVER.server?.server ?: return
        server.execute {
            val category = event.volumeCategory
            if (category is VolumeCategoryImpl) {
                val packet = AddCategoryPacket(category).toClientboundPacket()
                for (recorder in ReplayChunkRecorders.recorders()) {
                    recorder.recordIfVoicechatEnabled { packet }
                }
            }
        }
    }

    private fun onUnregisterCategory(event: UnregisterVolumeCategoryEvent) {
        val server = Voicechat.SERVER.server?.server ?: return
        server.execute {
            val packet = RemoveCategoryPacket(event.volumeCategory.id).toClientboundPacket()
            for (recorder in ReplayChunkRecorders.recorders()) {
                recorder.recordIfVoicechatEnabled { packet }
            }
        }
    }

    private fun onPlayerStateChanged(event: PlayerStateChangedEvent) {
        val voicechat = Voicechat.SERVER.server ?: return
        val server = voicechat.server ?: return
        server.execute {
            val state = voicechat.playerStateManager.getState(event.playerUuid)
            if (state != null) {
                val packet = PlayerStatePacket(state).toClientboundPacket()
                for (recorder in ReplayChunkRecorders.recorders()) {
                    recorder.recordIfVoicechatEnabled { packet }
                }
            }
        }
    }

    private fun <T: SoundPacket> recordForReceiver(
        event: PacketEvent<T>,
        packet: (ReplayFormat) -> Packet<ClientCommonPacketListener>
    ) {
        val player = event.receiverConnection?.getServerPlayer() ?: return
        player.levelServer.execute {
            for (recorder in ReplayPlayerRecorders.get(player)) {
                recorder.recordIfVoicechatEnabled { packet.invoke(recorder.format) }
            }
        }
    }

    private fun VoicechatConnection.getServerPlayer(): ServerPlayer? {
        return this.player.player as? ServerPlayer
    }

    private fun onPlayerRecorderSnapshot(event: ReplayPlayerRecorderSnapshotEvent) {
        val recorder = event.recorder
        if (!recorder.settings.recordVoiceChat) {
            return
        }

        this.recordAdditionalPackets(recorder)
    }

    private fun onChunkRecorderSnapshot(event: ReplayChunkRecorderSnapshotEvent) {
        val recorder = event.recorder
        if (!recorder.settings.recordVoiceChat) {
            return
        }

        this.recordAdditionalPackets(recorder)
    }

    private fun recordAdditionalPackets(recorder: ReplayRecorder) {
        val server = Voicechat.SERVER.server
        if (server != null) {
            val states = server.playerStateManager.states.associateBy { it.uuid }
            recorder.record(PlayerStatesPacket(states).toClientboundPacket())
            for (group in server.groupManager.groups.values) {
                recorder.record(AddGroupPacket(group.toClientGroup()).toClientboundPacket())
            }
            for (category in server.categoryManager.categories) {
                recorder.record(AddCategoryPacket(category).toClientboundPacket())
            }
        }
    }

    private inline fun ReplayRecorder.recordIfVoicechatEnabled(packet: () -> Packet<*>) {
        if (this.settings.recordVoiceChat) {
            this.record(packet.invoke())
        }
    }

    private fun de.maxhenkel.voicechat.net.Packet<*>.toClientboundPacket(): Packet<ClientCommonPacketListener> {
        return ClientboundCustomPayloadPacket(this)
    }
}