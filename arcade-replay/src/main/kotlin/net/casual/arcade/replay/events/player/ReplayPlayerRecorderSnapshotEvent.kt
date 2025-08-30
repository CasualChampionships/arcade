/*
 * Copyright (c) 2025 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.replay.events.player

import net.casual.arcade.events.common.Event
import net.casual.arcade.replay.recorder.player.ReplayPlayerRecorder

public data class ReplayPlayerRecorderSnapshotEvent(
    public val recorder: ReplayPlayerRecorder,
    public val initial: Boolean
): Event