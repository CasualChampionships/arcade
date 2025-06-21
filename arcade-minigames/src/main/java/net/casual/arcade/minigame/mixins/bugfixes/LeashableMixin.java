/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.minigame.mixins.bugfixes;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.mojang.datafixers.util.Either;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Leashable;
import net.minecraft.world.entity.decoration.LeashFenceKnotEntity;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
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
			target = "Lnet/minecraft/world/level/storage/ValueInput;read(Ljava/lang/String;Lcom/mojang/serialization/Codec;)Ljava/util/Optional;"
		)
	)
	@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
	private Optional<Leashable.LeashData> onReadLeashData(Optional<Leashable.LeashData> original, ValueInput input) {
		if (this instanceof Entity entity) {
			Optional<BlockPos> optional = input.read("LeashRelative", BlockPos.CODEC);
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
		ValueOutput output,
		@Nullable Leashable.LeashData leashData,
		CallbackInfo ci
	) {
		if (this instanceof Entity entity && leashData != null) {
			Entity holder = leashData.leashHolder;
			if (holder instanceof LeashFenceKnotEntity knot) {
				BlockPos relative = knot.getPos().subtract(entity.blockPosition());
				output.store("LeashRelative", BlockPos.CODEC, relative);
			}
		}
	}
}
