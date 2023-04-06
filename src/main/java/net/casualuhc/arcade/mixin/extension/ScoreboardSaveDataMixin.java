package net.casualuhc.arcade.mixin.extension;

import com.llamalad7.mixinextras.sugar.Local;
import net.casualuhc.arcade.extensions.Extension;
import net.casualuhc.arcade.utils.ExtensionUtils;
import net.casualuhc.arcade.utils.TeamUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
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
		method = "loadTeams",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/world/scores/ScoreboardSaveData;loadTeamPlayers(Lnet/minecraft/world/scores/PlayerTeam;Lnet/minecraft/nbt/ListTag;)V",
			shift = At.Shift.AFTER
		)
	)
	private void onLoadTeam(
		ListTag tagList,
		CallbackInfo ci,
		@Local PlayerTeam team,
		@Local CompoundTag tag
	) {
		CompoundTag arcade = tag.getCompound("arcade");
		for (Extension extension : TeamUtils.getExtensions(team)) {
			Tag data = arcade.get(extension.getName());
			if (data != null) {
				extension.deserialize(data);
			}
		}
	}

	@Inject(
		method = "saveTeams",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/nbt/ListTag;add(Ljava/lang/Object;)Z",
			ordinal = 1,
			shift = At.Shift.AFTER
		)
	)
	private void onSaveTeam(
		CallbackInfoReturnable<ListTag> cir,
		@Local PlayerTeam team,
		@Local CompoundTag tag
	) {
		CompoundTag arcade = new CompoundTag();
		for (Extension extension : TeamUtils.getExtensions(team)) {
			arcade.put(extension.getName(), extension.serialize());
		}
		tag.put("arcade", arcade);
	}
}
