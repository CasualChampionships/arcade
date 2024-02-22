package net.casual.arcade.utils

import me.senseiwells.replay.api.RejoinedPacketSender
import me.senseiwells.replay.api.ReplaySenders
import me.senseiwells.replay.chunk.ChunkRecorder
import me.senseiwells.replay.player.PlayerRecorder
import net.casual.arcade.utils.MinigameUtils.getMinigame
import net.casual.arcade.utils.ResourcePackUtils.toPushPacket
import net.fabricmc.loader.api.FabricLoader

internal object CompatabilityUtils {
    fun noop() {
        val loader = FabricLoader.getInstance()
        if (loader.isModLoaded("server-replay")) {
            this.enableReplayCompatability()
        }
    }

    private fun enableReplayCompatability() {
        ReplaySenders.addSender(MinigameRejoinedPacketSender)
    }

    private object MinigameRejoinedPacketSender: RejoinedPacketSender {
        // Important note: if you are implementing a MinigameEvent, then you must
        // handle additional resource packs yourself, we cannot detect that here!
        override fun recordAdditionalChunkPackets(recorder: ChunkRecorder) {
            val minigame = recorder.level.getMinigame() ?: return

            val packs = minigame.getResources().getPacks()
            for (pack in packs) {
                recorder.record(pack.toPushPacket())
            }
        }

        override fun recordAdditionalPlayerPackets(recorder: PlayerRecorder) {
            val player = recorder.getPlayerOrThrow()
            val minigame = player.getMinigame() ?: return

            // ServerReplay handles player resource packs

            // Resend all the minigame UI
            minigame.ui.resendUI(player, recorder::record)
            // TODO: Resend effects
        }
    }
}