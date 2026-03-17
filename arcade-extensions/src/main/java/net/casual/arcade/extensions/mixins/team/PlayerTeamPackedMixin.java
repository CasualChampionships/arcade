/*
 * Copyright (c) 2025 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.extensions.mixins.team;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.mojang.serialization.Codec;
import net.casual.arcade.extensions.ducks.ArcadeTeamDataHolder;
import net.casual.arcade.utils.serialization.codec.ArcadeExtraCodecs;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.scores.PlayerTeam;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Objects;
import java.util.Optional;

@Mixin(PlayerTeam.Packed.class)
public class PlayerTeamPackedMixin implements ArcadeTeamDataHolder {
    @Unique @Nullable private CompoundTag arcade$data = null;

    @ModifyExpressionValue(
        method = "<clinit>",
        at = @At(
            value = "INVOKE",
            target = "Lcom/mojang/serialization/codecs/RecordCodecBuilder;create(Ljava/util/function/Function;)Lcom/mojang/serialization/Codec;",
            remap = false
        )
    )
    private static Codec<PlayerTeam.Packed> extendCodecForArcade(Codec<PlayerTeam.Packed> original) {
        return ArcadeExtraCodecs.extend(
            original,
            CompoundTag.CODEC.optionalFieldOf("arcade").forGetter(packet -> {
                return Optional.<@NonNull CompoundTag>ofNullable(((ArcadeTeamDataHolder) (Object) packet).arcade_getData());
            }),
            (packed, tag) -> {
                ((ArcadeTeamDataHolder) (Object) packed).arcade_setData(tag.orElse(null));
                return packed;
            }
        );
    }

    @WrapMethod(method = "equals")
    private boolean checkArcadeDataIsEqual(Object object, Operation<Boolean> original) {
        return original.call(object) && object instanceof ArcadeTeamDataHolder holder
            && Objects.equals(this.arcade$data, holder.arcade_getData());
    }

    @Override
    @Nullable
    public CompoundTag arcade_getData() {
        return this.arcade$data;
    }

    @Override
    public void arcade_setData(@Nullable CompoundTag tag) {
        this.arcade$data = tag;
    }
}
