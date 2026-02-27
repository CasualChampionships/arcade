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
import de.maxhenkel.voicechat.api.packets.SoundPacket
import de.maxhenkel.voicechat.net.*
import de.maxhenkel.voicechat.plugins.impl.VolumeCategoryImpl
import net.casual.arcade.events.GlobalEventHandler
import net.casual.arcade.events.ListenerRegistry.Companion.register
import net.casual.arcade.events.server.ServerTickEvent
import net.casual.arcade.replay.events.chunk.ReplayChunkRecorderSnapshotEvent
import net.casual.arcade.replay.events.player.ReplayPlayerRecorderSnapshotEvent
import net.casual.arcade.replay.io.ReplayFormat
import net.casual.arcade.replay.recorder.ReplayRecorder
import net.casual.arcade.replay.recorder.chunk.ReplayChunkRecorders
import net.casual.arcade.replay.recorder.player.ReplayPlayerRecorders
import net.casual.arcade.utils.ArcadeUtils
import net.casual.arcade.utils.EnumUtils
import net.casual.arcade.utils.TimeUtils.Seconds
import net.minecraft.network.protocol.Packet
import net.minecraft.network.protocol.common.ClientCommonPacketListener
import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket
import net.minecraft.server.level.ServerPlayer
import net.minecraft.util.Util
import org.jetbrains.annotations.ApiStatus.Internal

@Internal
public object ReplayVoicechatPlugin: VoicechatPlugin {
    private val cache = VoicechatPacketCache()

    private lateinit var api: VoicechatApi

    override fun getPluginId(): String {
        return "${ArcadeUtils.MOD_ID}_replay_recorder"
    }

    override fun initialize(api: VoicechatApi) {
        this.api = api

        GlobalEventHandler.Server.register<ServerTickEvent>(this::onServerTick)
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
        this.recordForReceiver(event) { format, compress ->
            if (!compress) {
                this.cache.getOrCreate(event.voicechat, format, packet)
            } else {
                EncodedVoicechatPackets.get(format, packet)
            }
        }
    }

    private fun onEntitySoundPacket(event: EntitySoundPacketEvent) {
        if (event.isCancelled) {
            return
        }

        val packet = event.packet
        this.recordForReceiver(event) { format, compress ->
            if (!compress) {
                this.cache.getOrCreate(event.voicechat, format, packet)
            } else {
                EncodedVoicechatPackets.get(format, packet)
            }
        }
    }

    private fun onStaticSoundPacket(event: StaticSoundPacketEvent) {
        if (event.isCancelled) {
            return
        }

        val packet = event.packet
        this.recordForReceiver(event) { format, compress ->
            if (!compress) {
                this.cache.getOrCreate(event.voicechat, format, packet)
            } else {
                EncodedVoicechatPackets.get(format, packet)
            }
        }
    }

    private fun onMicrophonePacket(event: MicrophonePacketEvent) {
        val connection = event.senderConnection ?: return
        val player = connection.getServerPlayer() ?: return
        val grouped = connection.isInGroup
        val whispering = event.packet.isWhispering
        val distance = event.voicechat.voiceChatDistance.toFloat()

        val decodedMap = EnumUtils.mapOf<ReplayFormat, Packet<ClientCommonPacketListener>>()
        val encodedMap = EnumUtils.mapOf<ReplayFormat, Packet<ClientCommonPacketListener>>()
        val lazy: (ReplayFormat, Boolean) -> Packet<ClientCommonPacketListener> = { format, compress ->
            val data = event.packet.opusEncodedData
            if (!compress) {
                decodedMap.getOrPut(format) {
                    this.cache.create(event.voicechat, format, data, player.uuid, grouped, whispering, distance)
                }
            } else {
                encodedMap.getOrPut(format) {
                    EncodedVoicechatPackets.create(format, data, player.uuid, grouped, whispering, distance)
                }
            }
        }

        for (recorder in ReplayPlayerRecorders.get(player)) {
            recorder.recordIfVoicechatEnabled { lazy.invoke(recorder.format, recorder.shouldCompressVoicechat()) }
        }

        if (!grouped) {
            val dimension = player.level().dimension()
            val chunkPos = player.chunkPosition()
            for (recorder in ReplayChunkRecorders.containing(dimension, chunkPos)) {
                recorder.recordIfVoicechatEnabled { lazy.invoke(recorder.format, recorder.shouldCompressVoicechat()) }
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
        packet: (ReplayFormat, Boolean) -> Packet<ClientCommonPacketListener>
    ) {
        val player = event.receiverConnection?.getServerPlayer() ?: return
        for (recorder in ReplayPlayerRecorders.get(player)) {
            recorder.recordIfVoicechatEnabled { packet.invoke(recorder.format, recorder.shouldCompressVoicechat()) }
        }
    }

    private fun VoicechatConnection.getServerPlayer(): ServerPlayer? {
        return this.player.player as? ServerPlayer
    }

    private fun onServerTick(event: ServerTickEvent) {
        if (event.server.tickCount % 30.Seconds.ticks == 0) {
            this.cache.cleanUp()
        }
    }

    private fun onPlayerRecorderSnapshot(event: ReplayPlayerRecorderSnapshotEvent) {
        val recorder = event.recorder
        if (!recorder.settings.recordVoiceChat) {
            return
        }

        this.recordAdditionalPackets(recorder)

        // We have to send the client a secret for replay mod otherwise it won't play the packets
        if (recorder.format == ReplayFormat.ReplayMod) {
            val server = Voicechat.SERVER.server
            val player = recorder.getPlayerOrThrow()
            if (server != null && server.hasSecret(player.uuid)) {
                val secret = server.getSecret(player.uuid)
                val packet = SecretPacket(player, secret, server.port, Voicechat.SERVER_CONFIG)
                recorder.record(packet.toClientboundPacket())
            }
        }
    }

    private fun onChunkRecorderSnapshot(event: ReplayChunkRecorderSnapshotEvent) {
        val recorder = event.recorder
        if (!recorder.settings.recordVoiceChat) {
            return
        }

        this.recordAdditionalPackets(recorder)
        if (recorder.format == ReplayFormat.ReplayMod) {
            val server = Voicechat.SERVER.server
            if (server != null) {
                val player = recorder.getDummyPlayer()
                val packet = SecretPacket(player, server.generateNewSecret(Util.NIL_UUID), server.port, Voicechat.SERVER_CONFIG)
                recorder.record(packet.toClientboundPacket())
            }
        }
    }

    private fun recordAdditionalPackets(recorder: ReplayRecorder) {
        val server = Voicechat.SERVER.server
        if (server != null) {
            val states = server.playerStateManager.states
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