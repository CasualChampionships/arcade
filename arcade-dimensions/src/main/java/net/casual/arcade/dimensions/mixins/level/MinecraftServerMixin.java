package net.casual.arcade.dimensions.mixins.level;

import net.casual.arcade.dimensions.utils.LevelPersistenceTracker;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftServer.class)
public class MinecraftServerMixin {
	@Inject(
		method = "createLevels",
		at = @At("TAIL")
	)
	private void afterCreateVanillaLevels(CallbackInfo ci) {
		for (ResourceKey<Level> key : LevelPersistenceTracker.get()) {

		}
	}
}
