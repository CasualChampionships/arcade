package net.casual.arcade.events.client.mixins;

import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import net.casual.arcade.events.GlobalEventHandler;
import net.casual.arcade.events.client.render.LevelRenderEvent;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.renderer.LevelRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Set;

@Mixin(LevelRenderer.class)
public class LevelRendererMixin {
    @Inject(
        method = "method_62214",
        at = @At(value = "CONSTANT", args = "stringValue=terrain")
    )
    private void onTerrain(
        CallbackInfo ci,
        @Local(argsOnly = true) DeltaTracker deltas,
        @Share("event") LocalRef<LevelRenderEvent> eventRef
    ) {
        LevelRenderEvent event = new LevelRenderEvent((LevelRenderer) (Object) this, deltas);
        GlobalEventHandler.Client.broadcast(event, Set.of(LevelRenderEvent.TERRAIN));
        eventRef.set(event);
    }

    @Inject(
        method = "method_62214",
        at = @At(value = "CONSTANT", args = "stringValue=entities")
    )
    private void onEntities(CallbackInfo ci, @Share("event") LocalRef<LevelRenderEvent> eventRef) {
        GlobalEventHandler.Client.broadcast(eventRef.get(), Set.of(LevelRenderEvent.ENTITIES));
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

    @Inject(
        method = "method_62214",
        at = @At(value = "CONSTANT", args = "stringValue=translucent")
    )
    private void onTranslucent(CallbackInfo ci, @Share("event") LocalRef<LevelRenderEvent> eventRef) {
        GlobalEventHandler.Client.broadcast(eventRef.get(), Set.of(LevelRenderEvent.TRANSLUCENT));
    }
}
