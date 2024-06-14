package net.casual.arcade.mixin.bugfixes;

import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.datafixers.util.Either;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.decoration.HangingEntity;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Optional;
import java.util.UUID;

@Mixin(Mob.class)
public abstract class MobMixin extends Entity {
	@Shadow @Nullable private Either<UUID, BlockPos> delayedLeashInfo;

	public MobMixin(EntityType<?> entityType, Level level) {
		super(entityType, level);
	}

	@Inject(
		method = "addAdditionalSaveData",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/world/entity/decoration/HangingEntity;getPos()Lnet/minecraft/core/BlockPos;"
		)
	)
	private void addRelativePosition(CompoundTag compound, CallbackInfo ci, @Local HangingEntity entity) {
		BlockPos relative = entity.getPos().subtract(this.blockPosition());
		compound.put("LeashRelative", NbtUtils.writeBlockPos(relative));
	}

	@Inject(
		method = "readAdditionalSaveData",
		at = @At("TAIL")
	)
	private void readRelativePosition(CompoundTag compound, CallbackInfo ci) {
		if (compound.contains("LeashRelative", CompoundTag.TAG_INT_ARRAY)) {
			Optional<BlockPos> optional = NbtUtils.readBlockPos(compound, "LeashRelative");
			if (optional.isEmpty()) {
				return;
			}

			this.delayedLeashInfo = Either.right(this.blockPosition().offset(optional.get()));
		}
	}
}
