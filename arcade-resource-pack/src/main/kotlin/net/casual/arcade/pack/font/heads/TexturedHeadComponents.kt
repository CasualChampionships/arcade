/*
 * Copyright (c) 2025 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.pack.font.heads

import net.casual.arcade.utils.player.DynamicResolvableProfile
import net.casual.arcade.utils.player.StaticResolvableProfile
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.item.component.ResolvableProfile
import java.util.*

public interface TexturedHeadComponents {
    public fun getDefault(): Component

    public fun getHeadOrDefaultFor(resolvable: ResolvableProfile): Component

    public suspend fun getHeadFor(resolvable: ResolvableProfile): Component
}

public fun TexturedHeadComponents.getHeadOrDefaultFor(player: ServerPlayer): Component {
    return this.getHeadOrDefaultFor(StaticResolvableProfile(player.gameProfile))
}

public fun TexturedHeadComponents.getHeadOrDefaultFor(username: String): Component {
    return this.getHeadOrDefaultFor(DynamicResolvableProfile(username))
}

public fun TexturedHeadComponents.getHeadOrDefaultFor(uuid: UUID): Component {
    return this.getHeadOrDefaultFor(DynamicResolvableProfile(uuid))
}

public suspend fun TexturedHeadComponents.getHeadFor(player: ServerPlayer): Component {
    return this.getHeadFor(StaticResolvableProfile(player.gameProfile))
}

public suspend fun TexturedHeadComponents.getHeadFor(username: String): Component {
    return this.getHeadFor(DynamicResolvableProfile(username))
}

public suspend fun TexturedHeadComponents.getHeadFor(uuid: UUID): Component {
    return this.getHeadFor(DynamicResolvableProfile(uuid))
}