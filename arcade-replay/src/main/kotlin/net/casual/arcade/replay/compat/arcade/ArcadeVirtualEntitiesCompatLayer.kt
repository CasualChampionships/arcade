package net.casual.arcade.replay.compat.arcade

import net.casual.arcade.replay.recorder.chunk.ReplayChunkRecorder
import net.casual.arcade.replay.recorder.player.ReplayPlayerRecorder
import net.casual.arcade.utils.server.player
import net.casual.arcade.virtual.entity.observer.Observer
import net.casual.arcade.virtual.entity.utils.*
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.network.protocol.Packet
import net.minecraft.world.entity.Entity
import net.minecraft.world.phys.Vec3

internal object ArcadeVirtualEntitiesCompatLayer {
    val loaded: Boolean = FabricLoader.getInstance().isModLoaded("arcade-virtual-entities")

    fun resendObservingAttachments(recorder: ReplayPlayerRecorder) {
        if (this.loaded) {
            val player = recorder.server.player(recorder.recordingPlayerUUID) ?: return
            for (attachment in player.getObservingAttachments()) {
                attachment.resendTo(player.asObserver(), recorder::record)
            }
        }
    }

    @JvmStatic
    fun startObservingLevelAttachments(recorder: ReplayChunkRecorder) {
        if (this.loaded) {
            ChunkRecorderObserver(recorder).startObservingVirtualEntitiesIn(recorder.level)
        }
    }

    @JvmStatic
    fun stopObservingLevelAttachments(recorder: ReplayChunkRecorder) {
        if (this.loaded) {
            ChunkRecorderObserver(recorder).stopObservingVirtualEntitiesIn(recorder.level)
        }
    }

    @JvmStatic
    fun resendObservingLevelAttachments(recorder: ReplayChunkRecorder) {
        if (this.loaded) {
            val observer = ChunkRecorderObserver(recorder)
            for (attachment in recorder.level.getVirtualEntityAttachments()) {
                attachment.resendTo(observer)
            }
        }
    }

    @JvmStatic
    fun startObservingEntityAttachments(recorder: ReplayChunkRecorder, entity: Entity) {
        if (this.loaded) {
            ChunkRecorderObserver(recorder).startObservingVirtualEntitiesFor(entity)
        }
    }

    @JvmStatic
    fun stopObservingEntityAttachments(recorder: ReplayChunkRecorder, entity: Entity) {
        if (this.loaded) {
            ChunkRecorderObserver(recorder).stopObservingVirtualEntitiesFor(entity)
        }
    }

    @JvmStatic
    fun resendObservingEntityAttachments(recorder: ReplayChunkRecorder, entity: Entity) {
        if (this.loaded) {
            val observer = ChunkRecorderObserver(recorder)
            for (attachment in entity.getVirtualEntityAttachments()) {
                attachment.resendTo(observer)
            }
        }
    }

    private class ChunkRecorderObserver(
        private val recorder: ReplayChunkRecorder
    ): Observer {
        override fun position(): Vec3 {
            return this.recorder.position
        }

        override fun send(packet: Packet<*>) {
            this.recorder.record(packet)
        }

        override fun hashCode(): Int {
            return this.recorder.hashCode()
        }

        override fun equals(other: Any?): Boolean {
            return this === other || (other is ChunkRecorderObserver && this.recorder == other.recorder)
        }
    }
}