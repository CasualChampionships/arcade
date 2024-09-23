package net.casual.arcade.minigame.mixins;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.casual.arcade.minigame.ducks.OverridableColor;
import net.casual.arcade.minigame.utils.TeamUtilsKt;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.scores.PlayerTeam;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.HashSet;
import java.util.LinkedHashSet;

@Mixin(PlayerTeam.class)
public class PlayerTeamMixin implements OverridableColor {
	@Unique private int arcade$color = -1;

	@Redirect(
		method = "<init>",
		at = @At(
			value = "INVOKE",
			target = "Lcom/google/common/collect/Sets;newHashSet()Ljava/util/HashSet;",
			remap = false
		)
	)
	private <E> HashSet<E> onNewHashSet() {
		// Iterate players in a consistent order
		return new LinkedHashSet<>();
	}

	@ModifyReturnValue(
		method = {
			"getFormattedDisplayName",
			"getFormattedName"
		},
		at = @At("RETURN")
	)
	private MutableComponent modifyTeamColor(MutableComponent original) {
		Integer color = TeamUtilsKt.getHexColor((PlayerTeam) (Object) this);
		if (color != null) {
			original.withColor(color);
		}
		return original;
	}

	@Override
	public void arcade$setColor(int color) {
		this.arcade$color = color;
	}

	@Override
	public int arcade$getColor() {
		return this.arcade$color;
	}
}
