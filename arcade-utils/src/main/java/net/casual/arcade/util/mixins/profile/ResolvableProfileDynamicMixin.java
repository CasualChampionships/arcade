/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.util.mixins.profile;

import com.mojang.authlib.GameProfile;
import com.mojang.datafixers.util.Either;
import net.minecraft.server.players.ProfileResolver;
import net.minecraft.world.item.component.ResolvableProfile;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Mixin(ResolvableProfile.Dynamic.class)
public abstract class ResolvableProfileDynamicMixin {
    @Shadow @Final private Either<String, UUID> nameOrId;

    @Inject(
        method = "resolveProfile",
        at = @At("HEAD"),
        cancellable = true
    )
    private void tryQuicklyResolveProfile(
        ProfileResolver resolver,
        CallbackInfoReturnable<CompletableFuture<GameProfile>> cir
    ) {
        if (resolver instanceof ProfileResolver.Cached cached) {
            ProfileResolverCachedAccessor accessor = (ProfileResolverCachedAccessor) cached;
            Optional<GameProfile> profile = this.nameOrId.map(
                name -> accessor.accessProfileCacheByName().getIfPresent(name),
                uuid -> accessor.accessProfileCacheById().getIfPresent(uuid)
            );
            // If profile is null it is not present in the cache
            // otherwise it tried to load and was found to not exist
            // noinspection OptionalAssignedToNull
            if (profile != null) {
                // We need to do this because we cannot extend ResolvableProfile since it's sealed
                ResolvableProfile self = (ResolvableProfile) (Object) this;
                cir.setReturnValue(CompletableFuture.completedFuture(profile.orElse(self.partialProfile())));
            }
        }
    }
}
