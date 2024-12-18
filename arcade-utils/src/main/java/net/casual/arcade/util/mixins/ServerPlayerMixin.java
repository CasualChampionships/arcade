/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.util.mixins;

import net.casual.arcade.util.ducks.SilentRecipeSender;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.ServerRecipeBook;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayer.class)
public class ServerPlayerMixin implements SilentRecipeSender {
	@Shadow @Final private ServerRecipeBook recipeBook;
	@Unique
	private boolean arcade$silentRecipesDirty = false;

	@Inject(
		method = "tick",
		at = @At("TAIL")
	)

	private void onTick(CallbackInfo ci) {
		if (this.arcade$silentRecipesDirty) {
			this.arcade$silentRecipesDirty = false;
			this.recipeBook.sendInitialRecipeBook((ServerPlayer) (Object) this);
		}
	}

	@Override
	public void arcade$markSilentRecipesDirty() {
		this.arcade$silentRecipesDirty = true;
	}
}
