package net.casual.arcade.mixin.bugfixes;

import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.datafixers.util.Either;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Leashable;
import net.minecraft.world.entity.decoration.LeashFenceKnotEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

@Mixin(Leashable.class)
public interface LeashableMixin {
	@Inject(
		method = "readLeashData",
		at = @At("HEAD"),
		cancellable = true
	)
	private void onReadLeashData(CompoundTag compound, CallbackInfoReturnable<Leashable.LeashData> cir) {
		if (this instanceof Entity entity && compound.contains("LeashRelative", CompoundTag.TAG_INT_ARRAY)) {
			Optional<BlockPos> optional = NbtUtils.readBlockPos(compound, "LeashRelative");
			if (optional.isEmpty()) {
				return;
			}

			BlockPos position = entity.blockPosition().offset(optional.get());
			cir.setReturnValue(LeashDataInvoker.construct(Either.right(position)));
		}
	}

	@Inject(
		method = "writeLeashData",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/world/entity/decoration/LeashFenceKnotEntity;getPos()Lnet/minecraft/core/BlockPos;"
		)
	)
	private void onWriteLeashData(
		CompoundTag compound,
		Leashable.LeashData leashData,
		CallbackInfo ci,
		@Local LeashFenceKnotEntity leash
		) {
		if (this instanceof Entity entity) {
			BlockPos relative = leash.getPos().subtract(entity.blockPosition());
			compound.put("LeashRelative", NbtUtils.writeBlockPos(relative));
		}
	}
}
