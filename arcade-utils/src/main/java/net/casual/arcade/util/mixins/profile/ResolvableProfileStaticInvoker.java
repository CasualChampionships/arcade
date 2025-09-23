/*
 * Copyright (c) 2025 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.util.mixins.profile;

import com.mojang.authlib.GameProfile;
import com.mojang.datafixers.util.Either;
import net.minecraft.world.entity.player.PlayerSkin;
import net.minecraft.world.item.component.ResolvableProfile;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(ResolvableProfile.Static.class)
public interface ResolvableProfileStaticInvoker {
    @Invoker("<init>")
    static ResolvableProfile.Static create(Either<GameProfile, ResolvableProfile.Partial> either, PlayerSkin.Patch skin) {
        throw new AssertionError();
    }
}
