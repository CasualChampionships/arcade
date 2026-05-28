/*
 * Copyright (c) 2025 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.npc.mixins;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.llamalad7.mixinextras.sugar.Local;
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

    // Fixes issues with fake players taking knockback when they shouldn't,
    // this is really a bug with vanilla where players *should* take kb, but
    // it never actually gets synced to their client, so it seems like they don't.
    @WrapWithCondition(
        method = "blockedByItem",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/entity/LivingEntity;knockback(DDD)V"
        )
    )
    private boolean onKnockbackTarget(LivingEntity instance, double power, double xd, double zd) {
        return !(instance instanceof FakePlayer);
    }

    @WrapWithCondition(
        method = "hurtServer",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/entity/LivingEntity;knockback(DDD)V"
        )
    )
    private boolean onHurtKnockback(
        LivingEntity instance,
        double power,
        double xd,
        double zd,
        @Local(name = "blocked") boolean blocked
    ) {
        return !(instance instanceof FakePlayer) || !blocked;
    }
}
