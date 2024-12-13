package net.casual.arcade.events.client.mixins;

import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import com.mojang.blaze3d.vertex.PoseStack;
import net.casual.arcade.events.BuiltInEventPhases;
import net.casual.arcade.events.GlobalEventHandler;
import net.casual.arcade.events.client.render.LevelRenderEvent;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Set;

@Mixin(LevelRenderer.class)
public class LevelRendererMixin {
    @Inject(
        method = "method_62214",
        at = @At(value = "CONSTANT", args = "stringValue=entities")
    )
    private void onEntities(
        CallbackInfo ci,
        @Local(argsOnly = true) Camera camera,
        @Local(ordinal = 0) MultiBufferSource.BufferSource buffers,
        @Local(ordinal = 0) PoseStack stack,
        @Local(argsOnly = true) DeltaTracker deltas,
        @Share("event") LocalRef<LevelRenderEvent> eventRef
    ) {
        LevelRenderEvent event = new LevelRenderEvent((LevelRenderer) (Object) this, camera, buffers, stack, deltas);
        GlobalEventHandler.Client.broadcast(event, Set.of(LevelRenderEvent.ENTITIES, BuiltInEventPhases.DEFAULT));
        eventRef.set(event);
    }

    @Inject(
        method = "method_62214",
        at = @At(value = "CONSTANT", args = "stringValue=blockentities")
    )
    private void onBlockEntities(CallbackInfo ci, @Share("event") LocalRef<LevelRenderEvent> eventRef) {
        GlobalEventHandler.Client.broadcast(eventRef.get(), Set.of(LevelRenderEvent.BLOCK_ENTITIES));
    }

    @Inject(
        method = "method_62214",
        at = @At(value = "CONSTANT", args = "stringValue=debug")
    )
    private void onDebug(CallbackInfo ci, @Share("event") LocalRef<LevelRenderEvent> eventRef) {
        GlobalEventHandler.Client.broadcast(eventRef.get(), Set.of(LevelRenderEvent.DEBUG));
    }
}
