/*
 * Copyright (c) 2025 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.util.mixins.profile;

import com.mojang.authlib.properties.PropertyMap;
import net.minecraft.world.item.component.ResolvableProfile;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.Optional;
import java.util.UUID;

@Mixin(ResolvableProfile.Partial.class)
public interface ResolvableProfilePartialInvoker {
    @Invoker("<init>")
    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    static ResolvableProfile.Partial arcade_init(
        Optional<String> username,
        Optional<UUID> uuid,
        PropertyMap properties
    ) {
        throw new AssertionError();
    }
}
