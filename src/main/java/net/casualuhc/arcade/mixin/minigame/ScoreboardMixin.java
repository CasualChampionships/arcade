package net.casualuhc.arcade.mixin.minigame;

import net.minecraft.world.scores.Scoreboard;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.HashMap;
import java.util.LinkedHashMap;

@Mixin(Scoreboard.class)
public class ScoreboardMixin {
	@Redirect(
		method = "<init>",
		at = @At(
			value = "INVOKE",
			target = "Lcom/google/common/collect/Maps;newHashMap()Ljava/util/HashMap;",
			ordinal = 3,
			remap = false
		)
	)
	private <K, V> HashMap<K, V> onNewHashMap() {
		// Iterate teams in consistent order
		return new LinkedHashMap<>();
	}
}
