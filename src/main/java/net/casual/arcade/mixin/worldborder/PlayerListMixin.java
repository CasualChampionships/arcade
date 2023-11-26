package net.casual.arcade.mixin.worldborder;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.casual.arcade.utils.BorderUtils;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import net.minecraft.world.level.border.BorderChangeListener;
import net.minecraft.world.level.border.WorldBorder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(PlayerList.class)
public class PlayerListMixin {
	@ModifyArg(
		method = "addWorldborderListener",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/world/level/border/WorldBorder;addListener(Lnet/minecraft/world/level/border/BorderChangeListener;)V"
		)
	)
	private BorderChangeListener onAddListener(BorderChangeListener listener, @Local(argsOnly = true) ServerLevel level) {
		BorderUtils.addOriginalListener(level, listener);
		return listener;
	}

	@ModifyExpressionValue(
		method = "sendLevelInfo",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/server/level/ServerLevel;getWorldBorder()Lnet/minecraft/world/level/border/WorldBorder;"
		)
	)
	private WorldBorder onGetWorldBorder(WorldBorder border, ServerPlayer player, ServerLevel level) {
		if (BorderUtils.getSynced()) {
			return border;
		}
		WorldBorder current = level.getWorldBorder();
		// Minecraft by default scales down the center of the border,
		// so we need to copy the border and reset this adjustment...
		double scale = level.dimensionType().coordinateScale();
		if (scale != 1.0) {
			WorldBorder copy = new WorldBorder();
			copy.applySettings(current.createSettings());
			copy.setCenter(copy.getCenterX() * scale, copy.getCenterZ() * scale);
			return copy;
		}
		return current;
	}
}
