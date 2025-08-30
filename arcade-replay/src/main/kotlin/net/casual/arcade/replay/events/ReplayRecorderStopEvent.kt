/*
 * Copyright (c) 2025 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.replay.events

import net.casual.arcade.events.common.Event
import net.casual.arcade.replay.recorder.ReplayRecorder
import java.util.concurrent.CompletableFuture

public data class ReplayRecorderStopEvent(
    val recorder: ReplayRecorder,
    /**
     * Future which will be completed once the recorder has
     * closed, this future is running off-thread.
     */
    val closeFuture: CompletableFuture<Long>
): Event