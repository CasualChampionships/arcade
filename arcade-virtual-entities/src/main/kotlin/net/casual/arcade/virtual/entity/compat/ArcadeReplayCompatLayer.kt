package net.casual.arcade.virtual.entity.compat

import net.casual.arcade.events.GlobalEventHandler
import net.casual.arcade.events.utils.register
import net.casual.arcade.replay.events.player.ReplayPlayerRecorderSnapshotEvent
import net.casual.arcade.utils.server.player
import net.casual.arcade.virtual.entity.extensions.PlayerAttachmentObserverExtension.Companion.attachmentObserver
import net.fabricmc.loader.api.FabricLoader

public object ArcadeReplayCompatLayer {
    public val loaded: Boolean = FabricLoader.getInstance().isModLoaded("arcade-replay")

    internal fun registerReplaySnapshotAttachmentRecording() {
        GlobalEventHandler.Server.register<ReplayPlayerRecorderSnapshotEvent> { (recorder) ->
            val player = recorder.server.player(recorder.recordingPlayerUUID) ?: return@register
            for (attachment in player.attachmentObserver.attachments()) {
                attachment.resendTo(player, recorder::record)
            }
        }
    }
}