/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.virtual.entity.mixins;

import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.world.entity.Avatar;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.decoration.Mannequin;
import net.minecraft.world.item.component.ResolvableProfile;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Optional;

@Mixin(Mannequin.class)
public interface MannequinAccessor {
    @Accessor("DATA_PROFILE")
    static EntityDataAccessor<ResolvableProfile> accessProfileAccessor() {
        throw new AssertionError();
    }

    @Accessor("DATA_IMMOVABLE")
    static EntityDataAccessor<Boolean> accessImmovableAccessor() {
        throw new AssertionError();
    }

    @Accessor("DATA_DESCRIPTION")
    static EntityDataAccessor<Optional<Component>> accessDescriptionAccessor() {
        throw new AssertionError();
    }
}
