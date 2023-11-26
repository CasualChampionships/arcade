package net.casual.arcade.mixin.level;

import net.casual.arcade.utils.LevelUtils;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(Entity.class)
public abstract class EntityMixin {
	@Shadow public abstract Level level();

	@Redirect(
		method = "findDimensionEntryPoint",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/server/level/ServerLevel;dimension()Lnet/minecraft/resources/ResourceKey;"
		)
	)
	private ResourceKey<Level> getLikeDimension0(ServerLevel instance) {
		return LevelUtils.getLikeDimension(instance);
	}

	@Redirect(
		method = "findDimensionEntryPoint",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/world/level/Level;dimension()Lnet/minecraft/resources/ResourceKey;"
		)
	)
	private ResourceKey<Level> getLikeDimension1(Level instance) {
		return LevelUtils.getLikeDimension(instance);
	}

	@Redirect(
		method = "changeDimension",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/server/level/ServerLevel;dimension()Lnet/minecraft/resources/ResourceKey;"
		)
	)
	private ResourceKey<Level> getLikeDimension2(ServerLevel instance) {
		return LevelUtils.getLikeDimension(instance);
	}

	@ModifyVariable(
		method = "handleNetherPortal",
		at = @At("STORE")
	)
	private ResourceKey<Level> getOppositeDimension(ResourceKey<Level> original) {
		return LevelUtils.getNetherOppositeDimension(this.level(), original);
	}
}
