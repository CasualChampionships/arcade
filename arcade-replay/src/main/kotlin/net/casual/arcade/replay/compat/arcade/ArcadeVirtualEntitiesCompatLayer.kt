/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.replay.compat.arcade

import net.casual.arcade.observer.utils.asObserver
import net.casual.arcade.replay.recorder.chunk.ReplayChunkRecorder
import net.casual.arcade.replay.recorder.player.ReplayPlayerRecorder
import net.casual.arcade.utils.server.player
import net.casual.arcade.virtual.entity.ArcadeVirtualEntities
import net.casual.arcade.virtual.entity.utils.getObservingAttachments
import net.casual.arcade.virtual.entity.utils.getVirtualEntityAttachments
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.world.entity.Entity

internal object ArcadeVirtualEntitiesCompatLayer {
    val loaded: Boolean = FabricLoader.getInstance().isModLoaded(ArcadeVirtualEntities.MOD_ID)

    fun resendObservingAttachments(recorder: ReplayPlayerRecorder) {
        if (this.loaded) {
            val player = recorder.server.player(recorder.recordingPlayerUUID) ?: return
            for (attachment in player.getObservingAttachments()) {
                attachment.resendTo(player.asObserver(), recorder::record)
            }
        }
    }

    @JvmStatic
    fun resendObservingLevelAttachments(recorder: ReplayChunkRecorder) {
        if (this.loaded) {
            val observer = ArcadeObserversCompatLayer.observerFor(recorder)
            for (attachment in recorder.level.getVirtualEntityAttachments()) {
                attachment.resendTo(observer)
            }
        }
    }

    @JvmStatic
    fun resendObservingEntityAttachments(recorder: ReplayChunkRecorder, entity: Entity) {
        if (this.loaded) {
            val observer = ArcadeObserversCompatLayer.observerFor(recorder)
            for (attachment in entity.getVirtualEntityAttachments()) {
                attachment.resendTo(observer)
            }
        }
    }
}