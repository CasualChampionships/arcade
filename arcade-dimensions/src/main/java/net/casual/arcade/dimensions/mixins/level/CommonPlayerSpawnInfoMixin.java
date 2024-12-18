/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.dimensions.mixins.level;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.game.CommonPlayerSpawnInfo;
import net.minecraft.world.level.dimension.BuiltinDimensionTypes;
import net.minecraft.world.level.dimension.DimensionType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(CommonPlayerSpawnInfo.class)
public class CommonPlayerSpawnInfoMixin {
	@WrapOperation(
		method = "write",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/network/codec/StreamCodec;encode(Ljava/lang/Object;Ljava/lang/Object;)V"
		)
	)
	private void onEncodeDimensionType(
		StreamCodec<RegistryFriendlyByteBuf, Holder<DimensionType>> instance,
		Object buf,
		Object type,
		Operation<Void> original
	) {
		if (((Holder<?>) type).unwrapKey().isPresent()) {
			original.call(instance, buf, type);
			return;
		}

		Holder.Reference<DimensionType> replacement = ((RegistryFriendlyByteBuf) buf).registryAccess()
			.lookupOrThrow(Registries.DIMENSION_TYPE)
			.getOrThrow(BuiltinDimensionTypes.OVERWORLD);
		original.call(instance, buf, replacement);
	}
}
