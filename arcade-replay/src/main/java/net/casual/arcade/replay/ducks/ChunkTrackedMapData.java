/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.replay.ducks;

import net.casual.arcade.replay.recorder.chunk.ReplayChunkRecorder;
import net.casual.arcade.replay.recorder.chunk.map.ChunkRecorderMapTracker;

public interface ChunkTrackedMapData {
    ChunkRecorderMapTracker arcade_getTrackerForRecorder(ReplayChunkRecorder recorder);
}
