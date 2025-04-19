package net.casual.arcade.util.mixins.teams;

import com.llamalad7.mixinextras.sugar.Local;
import net.casual.arcade.util.ducks.OverridableColor;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Scoreboard;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Scoreboard.class)
public class ScoreboardMixin {
    @Inject(
        method = "loadPlayerTeam",
        at = @At("TAIL")
    )
    private void onLoadPlayerTeam(
        PlayerTeam.Packed packed,
        CallbackInfo ci,
        @Local PlayerTeam team
    ) {
        Integer color = ((OverridableColor) (Object) packed).arcade$getColor();
        ((OverridableColor) team).arcade$setColor(color);
    }
}
