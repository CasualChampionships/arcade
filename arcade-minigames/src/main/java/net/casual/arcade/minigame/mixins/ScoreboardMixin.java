package net.casual.arcade.minigame.mixins;

import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Scoreboard;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Scoreboard.class)
public class ScoreboardMixin {
	@Shadow @Mutable @Final private Object2ObjectMap<String, PlayerTeam> teamsByName;

	@Inject(
		method = "<init>",
		at = @At("TAIL")
	)
	private void makeLikedHashMap(CallbackInfo ci) {
		// Iterate teams in consistent order
		this.teamsByName = new Object2ObjectLinkedOpenHashMap<>();
	}
}
