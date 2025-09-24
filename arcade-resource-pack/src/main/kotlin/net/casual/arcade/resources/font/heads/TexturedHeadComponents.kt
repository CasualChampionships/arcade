/*
 * Copyright (c) 2025 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.resources.font.heads

import com.mojang.authlib.properties.PropertyMap
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.item.component.ResolvableProfile
import java.util.*

public interface TexturedHeadComponents {
    public fun getDefault(): Component

    public suspend fun getHeadFor(resolvable: ResolvableProfile): Component
}

public suspend fun TexturedHeadComponents.getHeadFor(player: ServerPlayer): Component {
    return this.getHeadFor(ResolvableProfile(player.gameProfile))
}

public suspend fun TexturedHeadComponents.getHeadFor(username: String): Component {
    return this.getHeadFor(ResolvableProfile(Optional.of(username), Optional.empty(), PropertyMap()))
}

public suspend fun TexturedHeadComponents.getHeadFor(uuid: UUID): Component {
    return this.getHeadFor(ResolvableProfile(Optional.empty(), Optional.of(uuid), PropertyMap()))
}