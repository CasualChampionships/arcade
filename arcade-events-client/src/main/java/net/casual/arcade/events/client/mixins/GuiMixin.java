/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.events.client.mixins;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.sugar.Local;
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

    @Definition(id = "graphics", local = @Local(type = GuiGraphicsExtractor.class, name = "graphics"))
    @Expression("graphics = ?")
    @Inject(
        method = "extractRenderState",
        at = @At(
            value = "MIXINEXTRAS:EXPRESSION",
            shift = At.Shift.AFTER
        )
    )
    private void onPreRender(
        DeltaTracker deltaTracker,
        boolean shouldRenderLevel,
        boolean resourcesLoaded,
        CallbackInfo ci,
        @Local(name = "graphics") GuiGraphicsExtractor graphics
    ) {
        if (this.minecraft.gui.screen() instanceof LevelLoadingScreen) {
            return;
        }

        GuiRenderEvent event = new GuiRenderEvent(graphics, deltaTracker);
        GlobalEventHandler.Client.broadcast(event, BuiltInEventPhases.PRE_PHASES);
    }

    @Inject(
        method = "extractRenderState",
        at = @At("TAIL")
    )
    private void onPostRender(
        DeltaTracker deltaTracker,
        boolean shouldRenderLevel,
        boolean resourcesLoaded,
        CallbackInfo ci,
        @Local(name = "graphics") GuiGraphicsExtractor graphics
    ) {
        if (this.minecraft.gui.screen() instanceof LevelLoadingScreen) {
            return;
        }

        GuiRenderEvent event = new GuiRenderEvent(graphics, deltaTracker);
        GlobalEventHandler.Client.broadcast(event, BuiltInEventPhases.POST_PHASES);
    }
}
