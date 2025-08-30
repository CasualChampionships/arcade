/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.minigame.compat

import net.casual.arcade.events.GlobalEventHandler
import net.casual.arcade.events.ListenerRegistry.Companion.register
import net.casual.arcade.minigame.utils.MinigameUtils.getMinigame
import net.casual.arcade.minigame.utils.MinigameUtils.getMinigames
import net.casual.arcade.replay.events.chunk.ReplayChunkRecorderSnapshotEvent
import net.casual.arcade.replay.events.player.ReplayPlayerRecorderSnapshotEvent
import net.casual.arcade.resources.utils.ResourcePackUtils.toPushPacket

internal object MinigamesReplayCompat {
    internal fun registerEvents() {
        GlobalEventHandler.Server.register<ReplayPlayerRecorderSnapshotEvent>(::onPlayerRecorderSnapshot)
        GlobalEventHandler.Server.register<ReplayChunkRecorderSnapshotEvent>(::onChunkRecorderSnapshot)
    }

    private fun onPlayerRecorderSnapshot(event: ReplayPlayerRecorderSnapshotEvent) {
        val recorder = event.recorder
        val player = recorder.getPlayerOrThrow()
        val minigame = player.getMinigame() ?: return

        // Resend all the minigame UI
        minigame.ui.resendUI(player, recorder::record)
    }

    // Important note: if you are implementing a MinigameEvent, then you must
    // handle additional resource packs yourself, we cannot detect that here!
    private fun onChunkRecorderSnapshot(event: ReplayChunkRecorderSnapshotEvent) {
        val recorder = event.recorder
        val minigames = recorder.level.getMinigames()
        for (minigame in minigames) {
            val packs = minigame.resources.getPacks()
            for (pack in packs) {
                recorder.record(pack.toPushPacket())
            }
        }
    }
}