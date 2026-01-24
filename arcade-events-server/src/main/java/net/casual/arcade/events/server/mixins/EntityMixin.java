/*
 * Copyright (c) 2025 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.events.server.mixins;

import net.casual.arcade.events.BuiltInEventPhases;
import net.casual.arcade.events.GlobalEventHandler;
import net.casual.arcade.events.server.entity.EntityTickEvent;
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({Entity.class, Display.class})
public class EntityMixin {
    @Inject(
        method = "tick",
        at = @At("HEAD")
    )
    private void onTickPre(CallbackInfo ci) {
        Entity self = (Entity) (Object) this;
        if (!self.level().isClientSide) {
            EntityTickEvent event = new EntityTickEvent(self);
            GlobalEventHandler.Server.broadcast(event, BuiltInEventPhases.PRE_PHASES);
        }
    }

    @Inject(
        method = "tick",
        at = @At("HEAD")
    )
    private void onTickPost(CallbackInfo ci) {
        Entity self = (Entity) (Object) this;
        if (!self.level().isClientSide) {
            EntityTickEvent event = new EntityTickEvent(self);
            GlobalEventHandler.Server.broadcast(event, BuiltInEventPhases.POST_PHASES);
        }
    }
}
