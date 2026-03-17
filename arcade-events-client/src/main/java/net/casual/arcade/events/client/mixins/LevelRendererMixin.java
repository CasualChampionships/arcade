/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.events.client.mixins;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.resource.GraphicsResourceAllocator;
import com.mojang.blaze3d.vertex.PoseStack;
import net.casual.arcade.events.BuiltInEventPhases;
import net.casual.arcade.events.GlobalEventHandler;
import net.casual.arcade.events.client.render.LevelRenderEvent;
import net.casual.arcade.events.client.render.LevelRenderExtractEvent;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.chunk.ChunkSectionsToRender;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.state.level.LevelRenderState;
import net.minecraft.util.profiling.ProfilerFiller;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4fc;
import org.joml.Vector4f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Objects;
import java.util.Set;

@Mixin(LevelRenderer.class)
public class LevelRendererMixin {
    @Shadow
    private @Nullable ClientLevel level;

    @Shadow
    @Final
    private LevelRenderState levelRenderState;

    @Inject(
        method = "renderLevel",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/util/profiling/ProfilerFiller;pop()V",
            ordinal = 0
        )
    )
    private void onExtractState(
        GraphicsResourceAllocator allocator,
        DeltaTracker deltas,
        boolean renderOutline,
        CameraRenderState cameraState,
        Matrix4fc modelViewMatrix,
        GpuBufferSlice terrainFog,
        Vector4f fogColor,
        boolean shouldRenderSky,
        ChunkSectionsToRender chunkSectionsToRender,
        CallbackInfo ci,
        @Local(name = "profiler") ProfilerFiller profiler,
        @Local(name = "cullFrustum") Frustum frustum
    ) {
        profiler.popPush("arcadeEvent");
        LevelRenderExtractEvent event = new LevelRenderExtractEvent(
            (LevelRenderer) (Object) this, Objects.requireNonNull(this.level), this.levelRenderState, cameraState, deltas, frustum
        );
        GlobalEventHandler.Client.broadcast(event);
    }

    @Inject(
        method = "lambda$addMainPass$0",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/renderer/LevelRenderer;submitEntities(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/state/level/LevelRenderState;Lnet/minecraft/client/renderer/SubmitNodeCollector;)V"
        )
    )
    private void onEntities(
        CallbackInfo ci,
        @Local(argsOnly = true) LevelRenderState state,
        @Local(name = "bufferSource") MultiBufferSource.BufferSource buffers,
        @Local(name = "poseStack") PoseStack stack,
        @Share("event") LocalRef<LevelRenderEvent> eventRef
    ) {
        LevelRenderEvent event = new LevelRenderEvent((LevelRenderer) (Object) this, state, buffers, stack);
        GlobalEventHandler.Client.broadcast(event, Set.of(LevelRenderEvent.ENTITIES, BuiltInEventPhases.DEFAULT));
        eventRef.set(event);
    }

    @Inject(
        method = "lambda$addMainPass$0",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/renderer/LevelRenderer;submitBlockEntities(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/state/level/LevelRenderState;Lnet/minecraft/client/renderer/SubmitNodeStorage;)V"
        )
    )
    private void onBlockEntities(CallbackInfo ci, @Share("event") LocalRef<LevelRenderEvent> eventRef) {
        GlobalEventHandler.Client.broadcast(eventRef.get(), Set.of(LevelRenderEvent.BLOCK_ENTITIES));
    }

    @Definition(id = "render", method = "Lnet/minecraft/client/renderer/gizmos/DrawableGizmoPrimitives;render(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;Lnet/minecraft/client/renderer/state/level/CameraRenderState;Lorg/joml/Matrix4fc;)V")
    @Expression("?.render(?, ?, ?, ?)")
    @Inject(
        method = "lambda$addMainPass$0",
        at = @At(value = "MIXINEXTRAS:EXPRESSION", shift = At.Shift.AFTER)
    )
    private void onDebug(CallbackInfo ci, @Share("event") LocalRef<LevelRenderEvent> eventRef) {
        GlobalEventHandler.Client.broadcast(eventRef.get(), Set.of(LevelRenderEvent.DEBUG));
    }
}
