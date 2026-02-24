/*
 * Copyright (c) 2025 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.utils.player

import com.mojang.authlib.GameProfile
import com.mojang.authlib.properties.PropertyMap
import com.mojang.datafixers.util.Either
import net.casual.arcade.util.mixins.profile.ResolvableProfileInvoker
import net.casual.arcade.util.mixins.profile.ResolvableProfilePartialInvoker
import net.casual.arcade.util.mixins.profile.ResolvableProfileStaticInvoker
import net.minecraft.server.players.ProfileResolver
import net.minecraft.world.entity.player.PlayerSkin
import net.minecraft.world.item.component.ResolvableProfile
import net.minecraft.world.item.component.ResolvableProfile.Dynamic
import net.minecraft.world.item.component.ResolvableProfile.Static
import java.util.*
import java.util.concurrent.CompletableFuture

@Suppress("FunctionName")
public fun DynamicResolvableProfile(username: String): ResolvableProfile {
    return ResolvableProfile.createUnresolved(username)
}

@Suppress("FunctionName")
public fun DynamicResolvableProfile(uuid: UUID): ResolvableProfile {
    return ResolvableProfile.createUnresolved(uuid)
}

@Suppress("FunctionName")
public fun StaticResolvableProfile(profile: GameProfile): ResolvableProfile {
    return ResolvableProfile.createResolved(profile)
}

@Suppress("FunctionName")
public fun StaticResolvableProfile(
    username: String? = null,
    uuid: UUID? = null,
    properties: PropertyMap = PropertyMap.EMPTY,
    skin: PlayerSkin.Patch = PlayerSkin.Patch.EMPTY
): ResolvableProfile {
    val partial = ResolvableProfilePartialInvoker.create(
        Optional.ofNullable(username),
        Optional.ofNullable(uuid),
        properties
    )
    return ResolvableProfileStaticInvoker.create(Either.right(partial), skin)
}

public fun ResolvableProfile.resolveProfileOrNull(resolver: ProfileResolver): CompletableFuture<GameProfile> {
    return when (this) {
        is Static -> this.resolveProfile(resolver)
        is Dynamic -> this.resolveProfile(resolver)
            .thenApply { profile -> if (profile === this.partialProfile()) null else profile }
    }
}

public fun ResolvableProfile.uuid(): Optional<UUID> {
    val either = (this as ResolvableProfileInvoker).invokeUnpack()
    return either.map({ Optional.of(it.id) }, { it.id })
}