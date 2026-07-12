/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.virtual.entity.compat

import net.casual.arcade.events.GlobalEventHandler
import net.casual.arcade.events.utils.register
import net.casual.arcade.replay.events.player.ReplayPlayerRecorderSnapshotEvent
import net.casual.arcade.utils.server.player
import net.casual.arcade.virtual.entity.extensions.PlayerAttachmentObserverExtension.Companion.attachmentObserverExtension
import net.casual.arcade.virtual.entity.utils.asObserver
import net.fabricmc.loader.api.FabricLoader

public object ArcadeReplayCompatLayer {
    public val loaded: Boolean = FabricLoader.getInstance().isModLoaded("arcade-replay")

    internal fun registerReplaySnapshotAttachmentRecording() {
        GlobalEventHandler.Server.register<ReplayPlayerRecorderSnapshotEvent> { (recorder) ->
            val player = recorder.server.player(recorder.recordingPlayerUUID) ?: return@register
            for (attachment in player.attachmentObserverExtension.attachments()) {
                attachment.resendTo(player.asObserver(), recorder::record)
            }
        }
    }
}