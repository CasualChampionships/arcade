package net.casual.arcade.mixin.level;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.casual.arcade.utils.LevelUtils;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(FallingBlockEntity.class)
public abstract class FallingBlockEntityMixin extends Entity {
	public FallingBlockEntityMixin(EntityType<?> entityType, Level level) {
		super(entityType, level);
	}

	@ModifyExpressionValue(
		method = "changeDimension",
		at = @At(
			value = "FIELD",
			target = "Lnet/minecraft/world/level/Level;END:Lnet/minecraft/resources/ResourceKey;"
		)
	)
	private ResourceKey<Level> replaceVanillaKey(ResourceKey<Level> original) {
		return LevelUtils.getEndDimensionFor(this.level(), original);
	}
}
