/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.events.client.mixins;

import com.llamalad7.mixinextras.sugar.Local;
import net.casual.arcade.events.GlobalEventHandler;
import net.casual.arcade.events.client.render.LevelRenderExtractEvent;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.extract.LevelExtractor;
import net.minecraft.client.renderer.state.level.LevelRenderState;
import net.minecraft.util.profiling.ProfilerFiller;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Objects;

@Mixin(LevelExtractor.class)
public class LevelExtractorMixin {
    @Shadow
    private @Nullable ClientLevel level;

    @Shadow
    @Final
    private LevelRenderState levelRenderState;

    @Inject(
        method = "extract",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/util/profiling/ProfilerFiller;pop()V",
            ordinal = 0
        )
    )
    private void onExtractState(
        DeltaTracker deltaTracker,
        Camera camera,
        float deltaPartialTick,
        CallbackInfo ci,
        @Local(name = "profiler") ProfilerFiller profiler,
        @Local(name = "cullFrustum") Frustum cullFrustum
    ) {
        profiler.popPush("arcadeEvent");
        LevelRenderExtractEvent event = new LevelRenderExtractEvent(
            (LevelExtractor) (Object) this, Objects.requireNonNull(this.level), this.levelRenderState, camera, deltaTracker, cullFrustum
        );
        GlobalEventHandler.Client.broadcast(event);
    }
}
