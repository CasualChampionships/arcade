/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.replay.mixins.chunk;

import net.casual.arcade.replay.ducks.ChunkTrackedMapData;
import net.casual.arcade.replay.recorder.chunk.ReplayChunkRecorder;
import net.casual.arcade.replay.recorder.chunk.map.ChunkRecorderMapTracker;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;
import java.util.WeakHashMap;

@Mixin(MapItemSavedData.class)
public class MapItemSavedDataMixin implements ChunkTrackedMapData {
    @Unique private final Map<ReplayChunkRecorder, ChunkRecorderMapTracker> arcade$recorderMapTrackers = new WeakHashMap<>();

    @Inject(
        method = "setColorsDirty",
        at = @At("TAIL")
    )
    private void onSetColorsDirty(int x, int y, CallbackInfo ci) {
        for (ChunkRecorderMapTracker tracker : this.arcade$recorderMapTrackers.values()) {
            tracker.markColorsDirty(x, y);
        }
    }

    @Inject(
        method = "setDecorationsDirty",
        at = @At("TAIL")
    )
    private void onSetDecorationsDirty(CallbackInfo ci) {
        for (ChunkRecorderMapTracker tracker : this.arcade$recorderMapTrackers.values()) {
            tracker.markDecorationsDirty();
        }
    }

    @Override
    public ChunkRecorderMapTracker arcade$getTrackerForRecorder(ReplayChunkRecorder recorder) {
        return this.arcade$recorderMapTrackers.computeIfAbsent(recorder, r -> {
            return new ChunkRecorderMapTracker((MapItemSavedData) (Object) this);
        });
    }
}
