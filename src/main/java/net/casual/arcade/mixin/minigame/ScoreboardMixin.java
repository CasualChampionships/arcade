package net.casual.arcade.mixin.minigame;

import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Scoreboard;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.LinkedHashMap;

@Mixin(Scoreboard.class)
public class ScoreboardMixin {
	@Shadow @Mutable @Final private Object2ObjectMap<String, PlayerTeam> teamsByName;

	@Redirect(
		method = "<init>",
		at = @At("TAIL")
	)
	private void makeLikedHashMap() {
		// Iterate teams in consistent order
		this.teamsByName = new Object2ObjectLinkedOpenHashMap<>();
	}
}
