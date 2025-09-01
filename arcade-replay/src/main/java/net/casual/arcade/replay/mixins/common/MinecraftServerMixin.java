/*
 * Copyright (c) 2025 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.replay.mixins.common;

import net.casual.arcade.replay.recorder.ReplayRecorder;
import net.casual.arcade.replay.recorder.chunk.ReplayChunkRecorder;
import net.casual.arcade.replay.recorder.chunk.ReplayChunkRecorders;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.ServerLinks;
import net.minecraft.server.players.PlayerList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.function.BooleanSupplier;

@Mixin(MinecraftServer.class)
public abstract class MinecraftServerMixin {
    @Unique private final Set<ReplayRecorder> arcade$paused = Collections.newSetFromMap(new WeakHashMap<>());

    @Shadow private int emptyTicks;

    @Shadow protected abstract int pauseWhileEmptySeconds();

    @Inject(
        method = "tickServer",
        at = @At("HEAD")
    )
    private void onPauseServer(BooleanSupplier hasTimeLeft, CallbackInfo ci) {
        int pauseWhileEmptyTicks = this.pauseWhileEmptySeconds() * 20;
        if (pauseWhileEmptyTicks <= 0) {
            return;
        }

        if (this.emptyTicks == pauseWhileEmptyTicks) {
            for (ReplayChunkRecorder recorder : ReplayChunkRecorders.recorders()) {
                recorder.pause(true);
                this.arcade$paused.add(recorder);
            }
        }

        if (this.emptyTicks == 0 && !this.arcade$paused.isEmpty()) {
            for (ReplayRecorder recorder : this.arcade$paused) {
                recorder.resume();
            }
            this.arcade$paused.clear();
        }
    }
}
