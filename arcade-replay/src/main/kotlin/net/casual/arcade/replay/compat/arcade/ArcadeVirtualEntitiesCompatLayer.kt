package net.casual.arcade.replay.compat.arcade

import net.casual.arcade.networking.utils.asObserver
import net.casual.arcade.replay.recorder.chunk.ReplayChunkRecorder
import net.casual.arcade.replay.recorder.player.ReplayPlayerRecorder
import net.casual.arcade.utils.server.player
import net.casual.arcade.virtual.entity.utils.*
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.world.entity.Entity

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
            recorder.observer.startObservingVirtualEntitiesIn(recorder.level)
        }
    }

    @JvmStatic
    fun stopObservingLevelAttachments(recorder: ReplayChunkRecorder) {
        if (this.loaded) {
            recorder.observer.stopObservingVirtualEntitiesIn(recorder.level)
        }
    }

    @JvmStatic
    fun resendObservingLevelAttachments(recorder: ReplayChunkRecorder) {
        if (this.loaded) {
            val observer = recorder.observer
            for (attachment in recorder.level.getVirtualEntityAttachments()) {
                attachment.resendTo(observer)
            }
        }
    }

    @JvmStatic
    fun startObservingEntityAttachments(recorder: ReplayChunkRecorder, entity: Entity) {
        if (this.loaded) {
            recorder.observer.startObservingVirtualEntitiesFor(entity)
        }
    }

    @JvmStatic
    fun stopObservingEntityAttachments(recorder: ReplayChunkRecorder, entity: Entity) {
        if (this.loaded) {
            recorder.observer.stopObservingVirtualEntitiesFor(entity)
        }
    }

    @JvmStatic
    fun resendObservingEntityAttachments(recorder: ReplayChunkRecorder, entity: Entity) {
        if (this.loaded) {
            val observer = recorder.observer
            for (attachment in entity.getVirtualEntityAttachments()) {
                attachment.resendTo(observer)
            }
        }
    }
}