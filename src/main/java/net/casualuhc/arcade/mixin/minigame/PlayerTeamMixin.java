package net.casualuhc.arcade.mixin.minigame;

import net.minecraft.world.scores.PlayerTeam;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.HashSet;
import java.util.LinkedHashSet;

@Mixin(PlayerTeam.class)
public class PlayerTeamMixin {
	@Redirect(
		method = "<init>",
		at = @At(
			value = "INVOKE",
			target = "Lcom/google/common/collect/Sets;newHashSet()Ljava/util/HashSet;"
		)
	)
	private <E> HashSet<E> onNewHashSet() {
		// Iterate players in a consistent order
		return new LinkedHashSet<>();
	}
}
