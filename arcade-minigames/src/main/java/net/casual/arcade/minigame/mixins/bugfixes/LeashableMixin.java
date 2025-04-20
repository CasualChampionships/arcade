/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.minigame.mixins.bugfixes;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.mojang.datafixers.util.Either;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Leashable;
import net.minecraft.world.entity.decoration.LeashFenceKnotEntity;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Optional;

@Mixin(Leashable.class)
public interface LeashableMixin {
    @ModifyExpressionValue(
		method = "readLeashData",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/nbt/CompoundTag;read(Ljava/lang/String;Lcom/mojang/serialization/Codec;)Ljava/util/Optional;"
		)
	)
	@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
	private Optional<Leashable.LeashData> onReadLeashData(Optional<Leashable.LeashData> original, CompoundTag compound) {
		if (this instanceof Entity entity) {
			Optional<BlockPos> optional = compound.read("LeashRelative", BlockPos.CODEC);
			if (optional.isPresent()) {
				BlockPos position = entity.blockPosition().offset(optional.get());
				return Optional.of(LeashDataInvoker.construct(Either.right(position)));
			}
		}
		return original;
	}

	@Inject(
		method = "writeLeashData",
		at = @At("HEAD")
	)
	private void onWriteLeashData(
		CompoundTag compound,
		@Nullable Leashable.LeashData leashData,
		CallbackInfo ci
	) {
		if (this instanceof Entity entity && leashData != null) {
			Entity holder = leashData.leashHolder;
			if (holder instanceof LeashFenceKnotEntity knot) {
				BlockPos relative = knot.getPos().subtract(entity.blockPosition());
				compound.store("LeashRelative", BlockPos.CODEC, relative);
			}
		}
	}
}
