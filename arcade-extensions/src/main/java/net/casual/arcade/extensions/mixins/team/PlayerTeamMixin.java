/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.extensions.mixins.team;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.casual.arcade.events.GlobalEventHandler;
import net.casual.arcade.extensions.ExtensionHolder;
import net.casual.arcade.extensions.ExtensionMap;
import net.casual.arcade.extensions.ducks.ArcadeTeamDataHolder;
import net.casual.arcade.extensions.event.TeamExtensionEvent;
import net.casual.arcade.utils.ArcadeUtils;
import net.casual.arcade.utils.ServerUtils;
import net.minecraft.world.level.storage.TagValueOutput;
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

	@ModifyReturnValue(
		method = "pack",
		at = @At("RETURN")
	)
	private PlayerTeam.Packed onPack(PlayerTeam.Packed original) {
		ArcadeUtils.scopedProblemReporter(reporter -> {
			TagValueOutput output = TagValueOutput.createWithContext(reporter, ServerUtils.getRegistryAccessOrEmpty());
			ExtensionHolder.serialize(this, output);
			((ArcadeTeamDataHolder) (Object) original).arcade$setData(output.buildResult());
		});
		return original;
	}

	@NotNull
	@Override
	@SuppressWarnings("AddedMixinMembersNamePattern")
	public ExtensionMap getExtensionMap() {
		return this.arcade$extensionMap;
	}
}
