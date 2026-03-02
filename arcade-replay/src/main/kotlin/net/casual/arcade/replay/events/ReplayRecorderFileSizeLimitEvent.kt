/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.replay.events

import net.casual.arcade.events.common.Event
import net.casual.arcade.replay.recorder.ReplayRecorder

public data class ReplayRecorderFileSizeLimitEvent(
    val recorder: ReplayRecorder
): Event