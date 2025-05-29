/*
 * Copyright (c) 2025 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.util.mixins.teams;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.mojang.serialization.Codec;
import net.casual.arcade.util.ducks.OverridableColor;
import net.casual.arcade.utils.codec.ArcadeExtraCodecs;
import net.minecraft.world.scores.PlayerTeam;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Optional;

@Mixin(PlayerTeam.Packed.class)
public class PlayerTeamPackedMixin implements OverridableColor {
    @Unique private Integer arcade$color = null;

    @Override
    public void arcade$setColor(@Nullable Integer color) {
        this.arcade$color = color;
    }

    @Override
    @Nullable
    public Integer arcade$getColor() {
        return this.arcade$color;
    }

    @ModifyExpressionValue(
        method = "<clinit>",
        at = @At(
            value = "INVOKE",
            target = "Lcom/mojang/serialization/codecs/RecordCodecBuilder;create(Ljava/util/function/Function;)Lcom/mojang/serialization/Codec;",
            remap = false
        )
    )
    private static Codec<PlayerTeam.Packed> extendWithOverridableColor(Codec<PlayerTeam.Packed> original) {
        return ArcadeExtraCodecs.extend(
            original,
            Codec.INT.optionalFieldOf("RawHexColor").forGetter(packed -> {
                return Optional.ofNullable(((OverridableColor) (Object) packed).arcade$getColor());
            }),
            (packed, color) -> {
                ((OverridableColor) (Object) packed).arcade$setColor(color.orElse(null));
                return packed;
            }
        );
    }
}
