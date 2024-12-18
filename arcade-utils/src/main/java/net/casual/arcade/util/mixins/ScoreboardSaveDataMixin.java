/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.util.mixins;

import com.llamalad7.mixinextras.sugar.Local;
import net.casual.arcade.util.ducks.OverridableColor;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.ScoreboardSaveData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ScoreboardSaveData.class)
public class ScoreboardSaveDataMixin {
	@Inject(
		method = "saveTeams",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/nbt/CompoundTag;putString(Ljava/lang/String;Ljava/lang/String;)V",
			ordinal = 0
		)
	)
	private void onSaveTeamData(
		HolderLookup.Provider levelRegistry,
		CallbackInfoReturnable<ListTag> cir,
		@Local PlayerTeam team,
		@Local CompoundTag tag
	) {
		Integer color = ((OverridableColor) team).arcade$getColor();
		if (color != null) {
			tag.putInt("RawHexColor", color);
		}
	}

	@Inject(
		method = "loadTeams",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/world/scores/ScoreboardSaveData;loadTeamPlayers(Lnet/minecraft/world/scores/PlayerTeam;Lnet/minecraft/nbt/ListTag;)V"
		)
	)
	private void onLoadTeamData(
		ListTag list,
		HolderLookup.Provider levelRegistry,
		CallbackInfo ci,
		@Local PlayerTeam team,
		@Local CompoundTag tag
	) {
		if (tag.contains("RawHexColor", ListTag.TAG_INT)) {
			int color = tag.getInt("RawHexColor");
			((OverridableColor) team).arcade$setColor(color);
		}
	}
}
