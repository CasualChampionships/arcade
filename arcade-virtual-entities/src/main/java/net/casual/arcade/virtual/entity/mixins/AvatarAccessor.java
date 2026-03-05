/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.virtual.entity.mixins;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.world.entity.Avatar;
import net.minecraft.world.entity.HumanoidArm;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Avatar.class)
public interface AvatarAccessor {
    @Accessor("DATA_PLAYER_MAIN_HAND")
    static EntityDataAccessor<HumanoidArm> accessPlayerMainHandAccessor() {
        throw new AssertionError();
    }

    // This is almost certainly meant to say MODEL lmao
    @Accessor("DATA_PLAYER_MODE_CUSTOMISATION")
    static EntityDataAccessor<Byte> accessPlayerModelCustomizationAccessor() {
        throw new AssertionError();
    }
}
