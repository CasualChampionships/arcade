/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.events.client.mixins;

import net.casual.arcade.events.phase.BuiltInEventPhases;
import net.casual.arcade.events.GlobalEventHandler;
import net.casual.arcade.events.client.ClientDisconnectEvent;
import net.casual.arcade.events.client.ClientStoppingEvent;
import net.casual.arcade.events.client.ClientTickEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public class MinecraftMixin {
    @Inject(
        method = "tick",
        at = @At("HEAD")
    )
    private void onPreTick(CallbackInfo ci) {
        ClientTickEvent event = new ClientTickEvent((Minecraft) (Object) this);
        GlobalEventHandler.Client.broadcast(event, BuiltInEventPhases.PRE_PHASES_RAW);
    }

    @Inject(
        method = "tick",
        at = @At("TAIL")
    )
    private void onPostTick(CallbackInfo ci) {
        ClientTickEvent event = new ClientTickEvent((Minecraft) (Object) this);
        GlobalEventHandler.Client.broadcast(event, BuiltInEventPhases.POST_PHASES_RAW);
    }

    @Inject(
        method = "stop",
        at = @At("HEAD")
    )
    private void onStop(CallbackInfo ci) {
        ClientStoppingEvent event = new ClientStoppingEvent((Minecraft) (Object) this);
        GlobalEventHandler.Client.broadcast(event);
    }

    @Inject(
        method = "disconnect(Lnet/minecraft/client/gui/screens/Screen;ZZ)V",
        at = @At("HEAD")
    )
    private void broadcastClientDisconnect(Screen screen, boolean keepResourcePacks, boolean stopSound, CallbackInfo ci) {
        ClientDisconnectEvent event = new ClientDisconnectEvent((Minecraft) (Object) this);
        GlobalEventHandler.Client.broadcast(event);
    }
}
