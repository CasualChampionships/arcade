/*
 * Copyright (c) 2025 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.extensions.mixins.compat;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import eu.pb4.polymer.common.impl.entity.InternalEntityHelpers;
import net.casual.arcade.extensions.EntityExtension;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@SuppressWarnings("UnstableApiUsage")
@Mixin(InternalEntityHelpers.class)
public class InternalEntityHelpersMixin {
    @WrapOperation(
        method = "getEntity",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/entity/EntityType;create(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/EntitySpawnReason;)Lnet/minecraft/world/entity/Entity;"
        )
    )
    private static <T extends Entity> T onTryCreateEntity(
        EntityType<T> instance,
        Level level,
        EntitySpawnReason spawnReason,
        Operation<T> original
    ) {
        boolean attach = EntityExtension.SHOULD_ATTACH_EXTENSION.get();
        try {
            EntityExtension.SHOULD_ATTACH_EXTENSION.set(false);
            return original.call(instance, level, spawnReason);
        } finally {
            EntityExtension.SHOULD_ATTACH_EXTENSION.set(attach);
        }
    }
}
