/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.events.client.mixins;

import net.casual.arcade.events.BuiltInEventPhases;
import net.casual.arcade.events.GlobalEventHandler;
import net.casual.arcade.events.client.render.GuiRenderEvent;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Gui.class)
public class GuiMixin {
    @Inject(
        method = "render",
        at = @At("HEAD")
    )
    private void onPreRender(GuiGraphics graphics, DeltaTracker deltas, CallbackInfo ci) {
        GuiRenderEvent event = new GuiRenderEvent(graphics, deltas);
        GlobalEventHandler.Client.broadcast(event, BuiltInEventPhases.PRE_PHASES);
    }

    @Inject(
        method = "render",
        at = @At("TAIL")
    )
    private void onPostRender(GuiGraphics graphics, DeltaTracker deltas, CallbackInfo ci) {
        GuiRenderEvent event = new GuiRenderEvent(graphics, deltas);
        GlobalEventHandler.Client.broadcast(event, BuiltInEventPhases.POST_PHASES);
    }
}
