/*
 * Copyright (c) 2025 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.extensions.utils

import net.casual.arcade.extensions.Extension
import net.casual.arcade.extensions.ExtensionHolder
import net.casual.arcade.extensions.ExtensionHolder.Companion.add
import net.casual.arcade.extensions.ExtensionHolder.Companion.get
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.entity.Entity
import net.minecraft.world.scores.PlayerTeam

public fun Entity.addExtension(extension: Extension) {
    (this as ExtensionHolder).add(extension)
}

public fun <T: Extension> Entity.getExtension(type: Class<T>): T {
    return (this as ExtensionHolder).get(type)
}

public inline fun <reified T: Extension> Entity.getExtension(): T {
    return this.getExtension(T::class.java)
}

public fun ServerLevel.addExtension(extension: Extension) {
    (this as ExtensionHolder).add(extension)
}

public fun <T: Extension> ServerLevel.getExtension(type: Class<T>): T {
    return (this as ExtensionHolder).get(type)
}

public inline fun <reified T: Extension> ServerLevel.getExtension(): T {
    return this.getExtension(T::class.java)
}

public fun PlayerTeam.addExtension(extension: Extension) {
    (this as ExtensionHolder).add(extension)
}

public fun <T: Extension> PlayerTeam.getExtension(type: Class<T>): T {
    return (this as ExtensionHolder).get(type)
}

public inline fun <reified T: Extension> PlayerTeam.getExtension(): T {
    return this.getExtension(T::class.java)
}