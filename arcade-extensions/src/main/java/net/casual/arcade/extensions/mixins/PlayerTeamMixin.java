/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.extensions.mixins;

import net.casual.arcade.events.GlobalEventHandler;
import net.casual.arcade.extensions.ExtensionHolder;
import net.casual.arcade.extensions.ExtensionMap;
import net.casual.arcade.extensions.event.TeamExtensionEvent;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Scoreboard;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerTeam.class)
public class PlayerTeamMixin implements ExtensionHolder {
	@Unique
	private final ExtensionMap arcade$extensionMap = new ExtensionMap();

	@Inject(
		method = "<init>",
		at = @At("TAIL")
	)
	private void onCreateTeam(Scoreboard scoreboard, String string, CallbackInfo ci) {
		TeamExtensionEvent event = new TeamExtensionEvent((PlayerTeam) (Object) this);
		GlobalEventHandler.Server.broadcast(event);
	}

	@NotNull
	@Override
	@SuppressWarnings("AddedMixinMembersNamePattern")
	public ExtensionMap getExtensionMap() {
		return this.arcade$extensionMap;
	}
}
