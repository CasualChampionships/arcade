/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.util.mixins.profile;

import com.google.common.cache.LoadingCache;
import com.mojang.authlib.GameProfile;
import net.minecraft.server.players.ProfileResolver;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Optional;
import java.util.UUID;

@Mixin(ProfileResolver.Cached.class)
public interface ProfileResolverCachedAccessor {
    @Accessor("profileCacheByName")
    LoadingCache<String, Optional<GameProfile>> accessProfileCacheByName();

    @Accessor("profileCacheById")
    LoadingCache<UUID, Optional<GameProfile>> accessProfileCacheById();
}
