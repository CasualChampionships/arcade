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
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.state.LevelRenderState;
import net.minecraft.util.profiling.ProfilerFiller;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
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
        boolean renderBlockOutline,
        Camera camera,
        Matrix4f frustumMatrix,
        Matrix4f projectionMatrix,
        Matrix4f cullingProjectionMatrix,
        GpuBufferSlice gpuBufferSlice,
        Vector4f vector4f,
        boolean addSkyPass,
        CallbackInfo ci,
        @Local ProfilerFiller profiler,
        @Local Frustum frustum
    ) {
        profiler.popPush("arcadeEvent");
        LevelRenderExtractEvent event = new LevelRenderExtractEvent(
            (LevelRenderer) (Object) this, Objects.requireNonNull(this.level), this.levelRenderState, camera, deltas, frustum
        );
        GlobalEventHandler.Client.broadcast(event);
    }

    @Inject(
        method = "method_62214",
        at = @At(value = "CONSTANT", args = "stringValue=submitEntities")
    )
    private void onEntities(
        CallbackInfo ci,
        @Local(argsOnly = true) LevelRenderState state,
        @Local(ordinal = 0) MultiBufferSource.BufferSource buffers,
        @Local(ordinal = 0) PoseStack stack,
        @Share("event") LocalRef<LevelRenderEvent> eventRef
    ) {
        DeltaTracker deltas = Minecraft.getInstance().getDeltaTracker();
        LevelRenderEvent event = new LevelRenderEvent((LevelRenderer) (Object) this, state, buffers, stack, deltas);
        GlobalEventHandler.Client.broadcast(event, Set.of(LevelRenderEvent.ENTITIES, BuiltInEventPhases.DEFAULT));
        eventRef.set(event);
    }

    @Inject(
        method = "method_62214",
        at = @At(value = "CONSTANT", args = "stringValue=submitBlockEntities")
    )
    private void onBlockEntities(CallbackInfo ci, @Share("event") LocalRef<LevelRenderEvent> eventRef) {
        GlobalEventHandler.Client.broadcast(eventRef.get(), Set.of(LevelRenderEvent.BLOCK_ENTITIES));
    }

    @Definition(id = "render", method = "Lnet/minecraft/client/renderer/gizmos/DrawableGizmoPrimitives;render(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;Lnet/minecraft/client/renderer/state/CameraRenderState;Lorg/joml/Matrix4f;)V")
    @Expression("?.render(?, ?, ?, ?)")
    @Inject(
        method = "method_62214",
        at = @At(value = "MIXINEXTRAS:EXPRESSION", shift = At.Shift.AFTER)
    )
    private void onDebug(CallbackInfo ci, @Share("event") LocalRef<LevelRenderEvent> eventRef) {
        GlobalEventHandler.Client.broadcast(eventRef.get(), Set.of(LevelRenderEvent.DEBUG));
    }
}
