/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.dimensions.mixins.level;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.game.CommonPlayerSpawnInfo;
import net.minecraft.world.level.dimension.BuiltinDimensionTypes;
import net.minecraft.world.level.dimension.DimensionType;
import org.jspecify.annotations.NonNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(CommonPlayerSpawnInfo.class)
public class CommonPlayerSpawnInfoMixin {
	@ModifyExpressionValue(
		method = "<clinit>",
		at = @At(
			value = "FIELD",
			target = "Lnet/minecraft/world/level/dimension/DimensionType;STREAM_CODEC:Lnet/minecraft/network/codec/StreamCodec;"
		)
	)
	private static StreamCodec<RegistryFriendlyByteBuf, Holder<DimensionType>> supportUnknownDimensionTypes(
		StreamCodec<RegistryFriendlyByteBuf, Holder<DimensionType>> original
	) {
		return new StreamCodec<>() {
			@NonNull
            @Override
			public Holder<DimensionType> decode(@NonNull RegistryFriendlyByteBuf input) {
                return original.decode(input);
            }

            @Override
            public void encode(@NonNull RegistryFriendlyByteBuf output, @NonNull Holder<DimensionType> value) {
				if (value.unwrapKey().isPresent()) {
					original.encode(output, value);
					return;
				}

				Holder.Reference<DimensionType> replacement = output.registryAccess()
					.getOrThrow(BuiltinDimensionTypes.OVERWORLD);
				original.encode(output, replacement);
            }
        };
	}
}
