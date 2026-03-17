/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.events.server.mixins;

import net.casual.arcade.events.GlobalEventHandler;
import net.casual.arcade.events.server.level.LevelTickEvent;
import net.minecraft.server.level.ServerLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.BooleanSupplier;

@Mixin(ServerLevel.class)
public class ServerLevelMixin {
	@Inject(
		method = "tick",
		at = @At("HEAD")
	)
	private void onTick(BooleanSupplier hasTimeLeft, CallbackInfo ci) {
		LevelTickEvent event = new LevelTickEvent((ServerLevel) (Object) this);
		GlobalEventHandler.Server.broadcast(event);
	}
}
