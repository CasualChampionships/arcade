/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.util.mixins.teams;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.casual.arcade.util.ducks.OverridableColor;
import net.casual.arcade.utils.TeamUtils;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.scores.PlayerTeam;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(PlayerTeam.class)
public class PlayerTeamMixin implements OverridableColor {
	@Unique @Nullable private Integer arcade$color = null;

	@ModifyReturnValue(
		method = {
			"getFormattedDisplayName",
			"getFormattedName"
		},
		at = @At("RETURN")
	)
	private MutableComponent modifyTeamColor(MutableComponent original) {
		return TeamUtils.color(original, (PlayerTeam) (Object) this);
	}

	@ModifyReturnValue(
		method = "pack",
		at = @At("RETURN")
	)
	private PlayerTeam.Packed onPackTeam(PlayerTeam.Packed original) {
		((OverridableColor) (Object) original).arcade$setColor(this.arcade$color);
		return original;
	}

	@Override
	public void arcade$setColor(@Nullable Integer color) {
		this.arcade$color = color;
	}

	@Override
	@Nullable
	public Integer arcade$getColor() {
		return this.arcade$color;
	}
}
