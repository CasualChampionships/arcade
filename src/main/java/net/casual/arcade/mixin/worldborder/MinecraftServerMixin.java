package net.casual.arcade.mixin.worldborder;

import com.llamalad7.mixinextras.sugar.Local;
import net.casual.arcade.utils.BorderUtils;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.border.BorderChangeListener;
import net.minecraft.world.level.border.WorldBorder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(MinecraftServer.class)
public class MinecraftServerMixin {
	@ModifyArg(
		method = "createLevels",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/world/level/border/WorldBorder;addListener(Lnet/minecraft/world/level/border/BorderChangeListener;)V"
		)
	)
	private BorderChangeListener onSyncListener(BorderChangeListener listener, @Local(ordinal = 1) ServerLevel serverLevel2) {
		BorderUtils.addOriginalListener(serverLevel2, listener);
		return listener;
	}
}
