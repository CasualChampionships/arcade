/*
 * Copyright (c) 2025 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.visuals.mixins;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(LivingEntity.class)
public class LivingEntityMixin {
    @ModifyExpressionValue(
        method = "collectEquipmentChanges",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/entity/LivingEntity;getItemBySlot(Lnet/minecraft/world/entity/EquipmentSlot;)Lnet/minecraft/world/item/ItemStack;"
        )
    )
    private ItemStack getOverlaidHeadSlot(ItemStack original, @Local EquipmentSlot slot) {
//        if (slot != EquipmentSlot.HEAD) {
//            return original;
//        }
//        if ((Object) this instanceof ServerPlayer player) {
//            PlayerCameraOverlayExtension extension = PlayerCameraOverlayExtension.getCameraOverlayExtension(player);
//            return extension.replaceStackWithOverlaid(original, player.registryAccess());
//        }
        return original;
    }
}
