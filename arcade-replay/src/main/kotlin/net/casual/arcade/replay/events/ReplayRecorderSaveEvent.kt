/*
 * Copyright (c) 2025 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.replay.events

import net.casual.arcade.events.common.ServerSideEvent
import net.casual.arcade.events.phase.BuiltInEventPhases
import net.casual.arcade.replay.recorder.ReplayRecorder
import java.nio.file.Path

public data class ReplayRecorderSaveEvent(
    val recorder: ReplayRecorder,
    val output: Path
): ServerSideEvent {
    public companion object {
        public const val PHASE_PRE: Int = BuiltInEventPhases.PRE

        public const val PHASE_POST: Int = BuiltInEventPhases.POST
    }
}