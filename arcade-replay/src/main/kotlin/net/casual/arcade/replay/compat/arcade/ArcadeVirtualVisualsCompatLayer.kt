/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.replay.compat.arcade

import net.casual.arcade.observer.utils.asObserver
import net.casual.arcade.replay.recorder.chunk.ReplayChunkRecorder
import net.casual.arcade.replay.recorder.player.ReplayPlayerRecorder
import net.casual.arcade.replay.util.asObserver
import net.casual.arcade.utils.server.player
import net.casual.arcade.virtual.visuals.ArcadeVirtualVisuals
import net.casual.arcade.virtual.visuals.utils.observingVisuals
import net.fabricmc.loader.api.FabricLoader

internal object ArcadeVirtualVisualsCompatLayer {
    val loaded: Boolean = FabricLoader.getInstance().isModLoaded(ArcadeVirtualVisuals.MOD_ID)

    fun resendObservingVisuals(recorder: ReplayPlayerRecorder) {
        if (this.loaded) {
            val player = recorder.server.player(recorder.recordingPlayerUUID) ?: return
            val observer = player.asObserver()
            for (visual in observer.observingVisuals().toList()) {
                visual.sendSpawnPackets(observer, recorder::record)
            }
        }
    }

    @JvmStatic
    fun resendObservingVisuals(recorder: ReplayChunkRecorder) {
        if (this.loaded) {
            val observer = recorder.asObserver()
            for (visual in observer.observingVisuals().toList()) {
                visual.sendSpawnPackets(observer, observer)
            }
        }
    }
}
