package net.casual.arcade.util.mixins;

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

	@Override
	public void arcade$setColor(Integer color) {
		this.arcade$color = color;
	}

	@Override
	public Integer arcade$getColor() {
		return this.arcade$color;
	}
}
