/*
 * Copyright (c) 2025 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.extensions.utils

import net.casual.arcade.extensions.Extension
import net.casual.arcade.extensions.ExtensionHolder
import net.casual.arcade.extensions.ExtensionHolder.Companion.add
import net.casual.arcade.extensions.ExtensionHolder.Companion.all
import net.casual.arcade.extensions.ExtensionHolder.Companion.get
import net.casual.arcade.utils.ArcadeUtils
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.entity.Entity
import net.minecraft.world.scores.PlayerTeam

public fun Entity.addExtension(extension: Extension) {
    (this as ExtensionHolder).add(extension)
}

public fun <T: Extension> Entity.getExtension(type: Class<T>): T {
    try {
        return (this as ExtensionHolder).get(type)
    } catch (exception: IllegalStateException) {
        val extensions = (this as ExtensionHolder).all()
        ArcadeUtils.logger.error("Failed to get extension for entity: $this", exception)
        ArcadeUtils.logger.error("Further details:")
        ArcadeUtils.logger.error("  Tick Count: ${this.tickCount}")
        ArcadeUtils.logger.error("  Passengers: ${this.passengers}")
        ArcadeUtils.logger.error("  Extensions: ${extensions.map { it::class.java.simpleName }}")
        throw exception
    }
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