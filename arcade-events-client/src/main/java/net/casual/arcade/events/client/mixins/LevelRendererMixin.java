/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.events.client.mixins;

import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import com.mojang.blaze3d.vertex.PoseStack;
import net.casual.arcade.events.phase.BuiltInEventPhases;
import net.casual.arcade.events.GlobalEventHandler;
import net.casual.arcade.events.client.render.LevelRenderEvent;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.state.level.LevelRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Set;

@Mixin(LevelRenderer.class)
public class LevelRendererMixin {
    @Inject(
        method = "submitFeatures",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/renderer/LevelRenderer;submitEntities(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/state/level/LevelRenderState;Lnet/minecraft/client/renderer/SubmitNodeCollector;)V"
        )
    )
    private void onEntities(
        CallbackInfo ci,
        @Local(name = "levelRenderState", argsOnly = true) LevelRenderState levelRenderState,
        @Local(name = "submitNodeCollector", argsOnly = true) SubmitNodeCollector submitNodeCollector,
        @Local(name = "poseStack") PoseStack poseStack,
        @Share("event") LocalRef<LevelRenderEvent> eventRef
    ) {
        LevelRenderEvent event = new LevelRenderEvent((LevelRenderer) (Object) this, levelRenderState, submitNodeCollector, poseStack);
        GlobalEventHandler.Client.broadcast(event, Set.of(LevelRenderEvent.ENTITIES, BuiltInEventPhases.DEFAULT));
        eventRef.set(event);
    }

    @Inject(
        method = "submitFeatures",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/renderer/LevelRenderer;submitBlockEntities(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/state/level/LevelRenderState;Lnet/minecraft/client/renderer/SubmitNodeCollector;)V"
        )
    )
    private void onBlockEntities(CallbackInfo ci, @Share("event") LocalRef<LevelRenderEvent> eventRef) {
        GlobalEventHandler.Client.broadcast(eventRef.get(), Set.of(LevelRenderEvent.BLOCK_ENTITIES));
    }

    @Inject(
        method = "submitFeatures",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/renderer/LevelRenderer;finalizeGizmoCollection()V"
        )
    )
    private void onDebug(CallbackInfo ci, @Share("event") LocalRef<LevelRenderEvent> eventRef) {
        GlobalEventHandler.Client.broadcast(eventRef.get(), Set.of(LevelRenderEvent.DEBUG));
    }
}
