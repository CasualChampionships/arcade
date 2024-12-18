/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.minigame.compat

import me.senseiwells.replay.api.ServerReplayPlugin
import me.senseiwells.replay.chunk.ChunkRecorder
import me.senseiwells.replay.player.PlayerRecorder
import net.casual.arcade.minigame.utils.MinigameUtils.getMinigame
import net.casual.arcade.minigame.utils.MinigameUtils.getMinigames
import net.casual.arcade.resources.utils.ResourcePackUtils.toPushPacket

public object ArcadeReplayPlugin: ServerReplayPlugin {
    override fun onPlayerReplayStart(recorder: PlayerRecorder) {
        val player = recorder.getPlayerOrThrow()
        val minigame = player.getMinigame() ?: return

        // Resend all the minigame UI
        minigame.ui.resendUI(player, recorder::record)
    }

    // Important note: if you are implementing a MinigameEvent, then you must
    // handle additional resource packs yourself, we cannot detect that here!
    override fun onChunkReplayStart(recorder: ChunkRecorder) {
        val minigames = recorder.level.getMinigames()
        for (minigame in minigames) {
            val packs = minigame.resources.getPacks()
            for (pack in packs) {
                recorder.record(pack.toPushPacket())
            }
        }
    }
}