/*
 * Copyright (c) 2025 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.npc.mixins;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.casual.arcade.npc.FakePlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(LivingEntity.class)
public class LivingEntityMixin {
    @ModifyExpressionValue(
        method = "<init>",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/entity/ai/attributes/DefaultAttributes;getSupplier(Lnet/minecraft/world/entity/EntityType;)Lnet/minecraft/world/entity/ai/attributes/AttributeSupplier;"
        )
    )
    private AttributeSupplier onGetAttributeSupplier(AttributeSupplier original) {
        if ((Object) this instanceof FakePlayer player) {
            return player.createAttributeSupplier();
        }
        return original;
    }
}
