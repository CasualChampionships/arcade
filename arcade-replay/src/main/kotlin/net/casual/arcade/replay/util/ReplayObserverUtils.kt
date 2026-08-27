/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.replay.util

import net.casual.arcade.observer.ArcadeObservers
import net.casual.arcade.observer.Observer
import net.casual.arcade.replay.compat.arcade.ArcadeObserversCompatLayer
import net.casual.arcade.replay.recorder.chunk.ReplayChunkRecorder

public fun ReplayChunkRecorder.asObserver(): Observer {
    require(ArcadeObserversCompatLayer.loaded) {
        "Cannot create observer for recorder as ${ArcadeObservers.MOD_ID} is not loaded"
    }
    return this.observer as Observer
}