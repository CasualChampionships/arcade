/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.events.client.mixins;

import net.casual.arcade.events.BuiltInEventPhases;
import net.casual.arcade.events.GlobalEventHandler;
import net.casual.arcade.events.client.render.GuiRenderEvent;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.LevelLoadingScreen;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Gui.class)
public class GuiMixin {
    @Shadow
    @Final
    private Minecraft minecraft;

    @Inject(
        method = "extractRenderState",
        at = @At("HEAD")
    )
    private void onPreRender(GuiGraphicsExtractor graphics, DeltaTracker deltas, CallbackInfo ci) {
        if (this.minecraft.screen instanceof LevelLoadingScreen) {
            return;
        }
        GuiRenderEvent event = new GuiRenderEvent(graphics, deltas);
        GlobalEventHandler.Client.broadcast(event, BuiltInEventPhases.PRE_PHASES);
    }

    @Inject(
        method = "extractRenderState",
        at = @At("TAIL")
    )
    private void onPostRender(GuiGraphicsExtractor graphics, DeltaTracker deltas, CallbackInfo ci) {
        if (this.minecraft.screen instanceof LevelLoadingScreen) {
            return;
        }
        GuiRenderEvent event = new GuiRenderEvent(graphics, deltas);
        GlobalEventHandler.Client.broadcast(event, BuiltInEventPhases.POST_PHASES);
    }
}
