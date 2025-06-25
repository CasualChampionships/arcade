/*
 * Copyright (c) 2025 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.npc.mixins;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(LivingEntity.class)
public interface LivingEntityAccessor {
    @Invoker
    AABB invokeGetHitbox();
}
